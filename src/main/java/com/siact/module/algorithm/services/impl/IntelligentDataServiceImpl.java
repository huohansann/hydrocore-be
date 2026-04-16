package com.siact.module.algorithm.services.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.config.KilnProperty;
import com.siact.common.exception.BizException;
import com.siact.common.redis.RedisService;
import com.siact.common.utils.JacksonUtils;
import com.siact.common.utils.TimeUtil;
import com.siact.module.algorithm.constants.AlgorithmConstant;
import com.siact.module.algorithm.dto.IntelliTplSettingDTO;
import com.siact.module.algorithm.dto.IntelliTplSettingDetailDTO;
import com.siact.module.algorithm.entity.IntelligentDataEntity;
import com.siact.module.algorithm.enums.IntelliTypeEnum;
import com.siact.module.algorithm.mapper.IntelligentDataMapper;
import com.siact.module.algorithm.services.AlgorithmService;
import com.siact.module.algorithm.services.IntelligentDataService;
import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.service.ControlIntervalConfigService;
import com.siact.module.system.constants.SysConfigCodeConstants;
import com.siact.module.system.dto.SysConfigDTO;
import com.siact.module.system.service.SysConfigService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2025-12-08 14:28
 * @className : IntelligentDataServiceImpl
 * @description : 智能计算算法业务类实现
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class IntelligentDataServiceImpl extends ServiceImpl<IntelligentDataMapper, IntelligentDataEntity> implements IntelligentDataService {
    private final AlgorithmService algorithmService;
    private final KilnProperty property;
    private final RedisService redis;
    private final SysConfigService sysConfigService;
    private final ControlIntervalConfigService configService;

    /**
     * 调用智能计算算法接口
     */
    @SuppressWarnings("unchecked")
    @Override
    public @Transactional void callIntelligentInterface() {
        // 将当前时间的秒归0处理
        LocalDateTime now = LocalDateTime.now().withSecond(0);
        log.info("开始获取智能计算数据,执行时间:{}", now.format(TimeUtil.df));

        // 封装参数
        JSONObject params = new JSONObject();

        SysConfigDTO intelliComputingParams = sysConfigService.getByCode(SysConfigCodeConstants.INTELLI_COMPUTING_PARAMS);
        Map<String, Object> icpData = (Map<String, Object>) intelliComputingParams.getData();

        params.put("ts", MapUtils.getString(icpData,"ts"));
        params.put("startTime", now.plusMinutes(-MapUtils.getInteger(icpData, "tracingTime")).format(TimeUtil.df));
        params.put("endTime", now.format(TimeUtil.df));
        Map<String, String> data = new HashMap<>();
        System.out.println(icpData.get("keyData"));
        List<Map<String, String>> keyData = (List<Map<String, String>>) icpData.get("keyData");
        keyData.forEach(item -> {
            data.put(MapUtils.getString(item,"algorithmCode"), MapUtils.getString(item,"dataCode"));
        });
        params.put("data", data);

        // 添加温度设定值参数
        SysConfigDTO controlTargetPoints = sysConfigService.getByCode(SysConfigCodeConstants.CONTROL_TARGET_POINTS);
        Map<String, String> cptData =(Map<String, String>) controlTargetPoints.getData();
        List<ControlIntervalConfigDTO> targetTemps = configService.selectListByDataCodeList(new ArrayList<>(cptData.values()));
        for (ControlIntervalConfigDTO dto : targetTemps) {
          params.put(dto.getMeasurePoint() + "_SP", new BigDecimal(dto.getTemperatureSet()));
        }

        Object cacheObject = redis.getCacheObject(AlgorithmConstant.INTELLI_ALGORITHM_CACHE_KEY);
        params.put("flag", ObjectUtils.isNotEmpty(cacheObject));


        // 调用接口
        JSONObject response;
        try {
            response = algorithmService.callResolve(
                    "control",
                    JSONObject.toJSONString(params),
                    () -> HttpUtil.post(property.getAlgorithm().getBaseUrl() + "/control", params.toJSONString(), property.getAlgorithm().getIntelligentTimeout())
            );
        } catch (BizException e) {
            log.error("智能控制算法调用异常: {}", e.getMessage());
            return;
        }
        // 响应时间
        String time = TimeUtil.getNow();

        JSONObject rs = response.getJSONObject("result");
        JSONObject result = rs.getJSONObject("result1");
        // 获取模型训练结果
        BigDecimal modelTransformer = result.getBigDecimal("gas_setValue_transformer");
        BigDecimal modelDeltaC = result.getBigDecimal("gas_deltaC_transformer");
        // 获取专家经验结果
        BigDecimal expertDeltaC = getValueInJson("experience_result", "delta_C", result);
        // 获取上次流量总和
        BigDecimal lastGasSum = result.getBigDecimal("last_gas_sum_sv");

        // 获取编码数据
        SysConfigDTO intelliOutputDataCode = sysConfigService.getByCode(SysConfigCodeConstants.INTELLI_OUTPUT_DATACODE);
        // 获取点位名称编码
        IntelliTplSettingDTO dto = JacksonUtils.fromJson(JacksonUtils.toJson(intelliOutputDataCode.getData()), IntelliTplSettingDTO.class);
        IntelliTplSettingDetailDTO detailDTO = dto.getDataCodeList().stream().filter(IntelliTplSettingDetailDTO::getActive).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(detailDTO)) return;

        IntelligentDataEntity.IntelligentDataEntityBuilder builder = IntelligentDataEntity.builder().name(detailDTO.getName()).dataCode(detailDTO.getDataCode()).time(time).data(JacksonUtils.toJson(response));

        ArrayList<IntelligentDataEntity> collect = new ArrayList<>();
        collect.add(builder.intelliType(IntelliTypeEnum.GAS_TRANSFORMER_MODEL).val(modelTransformer).build());
        collect.add(builder.intelliType(IntelliTypeEnum.GAS_DELTAC_MODEL).val(modelDeltaC).build());
        collect.add(builder.intelliType(IntelliTypeEnum.GAS_DELTAC_EXPERT).val(expertDeltaC).build());
        collect.add(builder.intelliType(IntelliTypeEnum.GAS_LAST_SUM).val(lastGasSum).build());
        // 保存数据
        saveBatch(collect);

        // 对比数据, deltaC 变动则设置缓存, 调整 flag 参数
        if (!BigDecimal.ZERO.equals(expertDeltaC)) {
            // 设置 cache 四十分钟过期
            redis.setCacheObject(AlgorithmConstant.INTELLI_ALGORITHM_CACHE_KEY, lastGasSum.add(expertDeltaC), property.getAlgorithm().getIntelliStopInterval(), TimeUnit.MINUTES);
        }
    }

    private BigDecimal getValueInJson(String mc, String key, JSONObject resultJson) {
        JSONObject json = resultJson.getJSONObject(mc);
        String[] keys = key.split("\\.");
        key = keys[keys.length - 1];
        for (String s : Arrays.copyOf(keys, keys.length - 1)) {
            json = json.getJSONObject(s);
        }
        return json.getBigDecimal(key);
    }
}
