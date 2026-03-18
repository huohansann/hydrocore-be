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
import com.siact.module.base.service.TplService;
import com.siact.module.base.vo.TplVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final TplService tplService;
    private final RedisService redis;
    // private final ControlIntervalConfigService configService;

    /**
     * 调用智能计算算法接口
     */
    @Override
    public @Transactional void callIntelligentInterface() {
        // 将当前时间的秒归0处理
        LocalDateTime now = LocalDateTime.now().withSecond(0);
        log.info("开始获取智能计算数据,执行时间:{}", now.format(TimeUtil.df));

        // 封装参数
        JSONObject params = new JSONObject();
        // params.put("fire_change_cycle", property.getConfig().getFireChangeCycle());
        // params.put("model", "LightGBM2");
        // params.put("method", "model");

        JSONObject intelligentComputingParams = JSONObject.parseObject(tplService.selectTplByCode("intelligentComputingParams").getTplContent());
        params.put("ts", intelligentComputingParams.getString("ts"));
        params.put("startTime", now.plusMinutes(-intelligentComputingParams.getInteger("tracingTime")).format(TimeUtil.df));
        params.put("endTime", now.format(TimeUtil.df));
        JSONObject data = new JSONObject();
        intelligentComputingParams.getJSONArray("keyData").forEach(o -> {
            JSONObject itemJson = JSONObject.from(o);
            data.put(itemJson.getString("algorithmCode"), itemJson.getString("dataCode"));
        });
        params.put("data", data);
        params.put("TE213_SP", intelligentComputingParams.getBigDecimal("TE213_SP"));
        params.put("TE202_SP", intelligentComputingParams.getBigDecimal("TE202_SP"));
        params.put("TE206_SP", intelligentComputingParams.getBigDecimal("TE206_SP"));
        Object cacheObject = redis.getCacheObject(AlgorithmConstant.INTELLI_ALGORITHM_CACHE_KEY);
        params.put("flag", ObjectUtils.isNotEmpty(cacheObject));

        // for (int i = 1; i <= 10; i++) {
        //     String mc = "MC" + i;
        //     ControlIntervalConfigDTO dto = configService.get(ControlIntervalConfigVO.builder().measurePoint(mc).build());
        //     // MC 温度上限
        //     params.put(mc + "_MAX_THRESHOLD", Double.valueOf(dto.getUpControl()));
        //     // MC 温度下限
        //     params.put(mc + "_MIN_THRESHOLD", Double.valueOf(dto.getLowControl()));
        //     // MC 控制目标
        //     params.put(mc + "_CONTROL_TARGET", Double.valueOf(dto.getTemperatureSet()));
        // }

        // 调用接口
        JSONObject response;
        try {
            response = algorithmService.callResolve(
                    "control",
                    JSONObject.toJSONString(params),
                    () -> HttpUtil.post(property.getAlgorithm().getBaseUrl() + "/control", params.toJSONString(), 600000)
            );
        } catch (BizException e) {
            log.error("智能控制算法调用异常: {}", e.getMessage());
            return;
        }
        // 响应时间
        String time = TimeUtil.getNow();

        JSONObject result = response.getJSONObject("result");
        // 获取模型训练结果
        BigDecimal modelTransformer = result.getBigDecimal("gas_setValue_transformer");
        BigDecimal modelDeltaC = result.getBigDecimal("gas_deltaC_transformer");
        // 获取专家经验结果
        BigDecimal expertDeltaC = getValueInJson("experience_result", "delta_C", result);
        // 获取上次流量总和
        BigDecimal lastGasSum = result.getBigDecimal("last_gas_sum_sv");

        // 获取编码数据
        TplVO intelliOutputDataCode = tplService.selectTplByCode("intelliOutputDataCode");
        // 获取点位名称编码
        IntelliTplSettingDTO dto = JacksonUtils.fromJson(intelliOutputDataCode.getTplContent(), IntelliTplSettingDTO.class);
        IntelliTplSettingDetailDTO detailDTO = dto.getDataCodeList().get(0);

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
