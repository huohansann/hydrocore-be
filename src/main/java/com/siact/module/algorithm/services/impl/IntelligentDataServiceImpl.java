package com.siact.module.algorithm.services.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.config.KilnProperty;
import com.siact.common.exception.BizException;
import com.siact.common.utils.TimeUtil;
import com.siact.module.algorithm.dto.IntelliTplSettingDTO;
import com.siact.module.algorithm.dto.IntelliTplSettingDetailDTO;
import com.siact.module.algorithm.entity.IntelligentDataEntity;
import com.siact.module.algorithm.enums.IntelliTypeEnum;
import com.siact.module.algorithm.mapper.IntelligentDataMapper;
import com.siact.module.algorithm.services.AlgorithmService;
import com.siact.module.algorithm.services.IntelligentDataService;
import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.service.ControlIntervalConfigService;
import com.siact.module.base.service.TplService;
import com.siact.module.base.vo.ControlIntervalConfigVO;
import com.siact.module.base.vo.TplVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-08 14:28
 * @className : IntelligentDataServiceImpl
 * @description : 智能计算算法业务类实现
 */
@Slf4j
@Service
public class IntelligentDataServiceImpl extends ServiceImpl<IntelligentDataMapper, IntelligentDataEntity> implements IntelligentDataService {
    private final AlgorithmService algorithmService;
    private final KilnProperty property;
    private final TplService tplService;
    private final ControlIntervalConfigService configService;

    public IntelligentDataServiceImpl(AlgorithmService algorithmService, KilnProperty property, TplService tplService, ControlIntervalConfigService configService) {
        this.algorithmService = algorithmService;
        this.property = property;
        this.tplService = tplService;
        this.configService = configService;
    }

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
        params.put("fire_change_cycle", property.getConfig().getFireChangeCycle());
        params.put("model", "LightGBM2");
        params.put("method", "model");

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

        for (int i = 1; i <= 10; i++) {
            String mc = "MC" + i;
            ControlIntervalConfigDTO dto = configService.get(ControlIntervalConfigVO.builder().measurePoint(mc).build());
            // MC 温度上限
            params.put(mc + "_MAX_THRESHOLD", Double.valueOf(dto.getUpControl()));
            // MC 温度下限
            params.put(mc + "_MIN_THRESHOLD", Double.valueOf(dto.getLowControl()));
            // MC 控制目标
            params.put(mc + "_CONTROL_TARGET", Double.valueOf(dto.getTemperatureSet()));
        }

        // 调用接口
        JSONObject response;
        try {
            response = algorithmService.callResolve(
                    "control",
                    JSONObject.toJSONString(params),
                    () -> HttpUtil.post(property.getAlgorithm().getBaseUrl() + "/control", params.toJSONString(), 600000)
            );
        } catch (BizException e) {
            return;
        }
        // 响应时间
        String time = TimeUtil.getNow();

        JSONObject result = response.getJSONObject("result");
        // 基于 model
        JSONObject modelJson = result.getJSONObject("Model");
        // 基于专家经验
        JSONObject expertJson = result.getJSONObject("Experience");
        // 运行值
        JSONArray lastGasSetValue = modelJson.getJSONArray("last_gasSetValue");

        // 获取编码数据
        TplVO intelliOutputDataCode = tplService.selectTplByCode("intelliOutputDataCode");
        Map<String, IntelliTplSettingDTO> dto = JSON.parseArray(intelliOutputDataCode.getTplContent(), IntelliTplSettingDTO.class).stream().collect(Collectors.toMap(IntelliTplSettingDTO::getType, o -> o, (v1, v2) -> v1));
        Map<String, IntelliTplSettingDetailDTO> tempTplDTO = dto.get("TEMP").getDataCodeList().stream().collect(Collectors.toMap(IntelliTplSettingDetailDTO::getName, o -> o, (v1, v2) -> v1));
        Map<String, IntelliTplSettingDetailDTO> gasTplDTO = dto.get("GAS").getDataCodeList().stream().collect(Collectors.toMap(IntelliTplSettingDetailDTO::getName, o -> o, (v1, v2) -> v1));

        ArrayList<IntelligentDataEntity> collect = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String mc = "MC" + i;
            IntelliTplSettingDetailDTO tempDTO = tempTplDTO.get(mc);
            IntelligentDataEntity.IntelligentDataEntityBuilder builder = IntelligentDataEntity.builder().name(tempDTO.getName()).dataCode(tempDTO.getDataCode()).time(time).data(modelJson.toJSONString());
            // 预测最大、最小温度
            collect.add(builder.intelliType(IntelliTypeEnum.MIN_TEMP).val(getValueInJson(mc, "min_temp", modelJson)).build());
            collect.add(builder.intelliType(IntelliTypeEnum.MAX_TEMP).val(getValueInJson(mc, "max_temp", modelJson)).build());

            // 天然气智控值只需要 8 个点位
            if (i > 8) continue;
            // gas 智控值
            String key = i + "#";
            IntelliTplSettingDetailDTO gasDTO = gasTplDTO.get(key);
            builder = IntelligentDataEntity.builder().name(gasDTO.getName()).dataCode(gasDTO.getDataCode()).time(time);
            // model
            collect.add(builder.intelliType(IntelliTypeEnum.GAS_CALC_MODEL1).val(getValueInJson(mc, "method1.delta_C", modelJson)).data(modelJson.toJSONString()).build());
            collect.add(builder.intelliType(IntelliTypeEnum.GAS_CALC_MODEL2).val(getValueInJson(mc, "method2.delta_C", modelJson)).data(modelJson.toJSONString()).build());
            // expert
            collect.add(builder.intelliType(IntelliTypeEnum.GAS_CALC_EXPERT1).val(getValueInJson(mc, "method1.delta_C", expertJson)).data(expertJson.toJSONString()).build());
            collect.add(builder.intelliType(IntelliTypeEnum.GAS_CALC_EXPERT2).val(getValueInJson(mc, "method2.delta_C", expertJson)).data(expertJson.toJSONString()).build());
            // 运行值
            collect.add(builder.intelliType(IntelliTypeEnum.GAS_RUN_VALUE).val(lastGasSetValue.getBigDecimal(i - 1)).data(modelJson.toJSONString()).build());
        }
        // 保存数据
        saveBatch(collect);
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

    /**
     * 获取最后的天然气智控计算值
     */
    @Override
    public Map<String, IntelligentDataEntity> lastGasCalc() {
        Map<String, Map<IntelliTypeEnum, IntelligentDataEntity>> map = this.queryByTypeWithLastTime(IntelliTypeEnum.GAS_RUN_VALUE, IntelliTypeEnum.GAS_CALC_MODEL2);

        Map<String, IntelligentDataEntity> result = new HashMap<>();

        map.forEach((k, v) -> {
            IntelligentDataEntity runValue = v.get(IntelliTypeEnum.GAS_RUN_VALUE);
            IntelligentDataEntity intelliModelValue = v.get(IntelliTypeEnum.GAS_CALC_MODEL2);
            // 计算智控值
            runValue.setVal(runValue.getVal().add(intelliModelValue.getVal()));
            result.put(k, runValue);
        });

        return result;
    }

    /**
     * 获取指定类型最后的时间点的智能算法值
     *
     * @param types 要查询的智能算法值类型
     * @return 返回 key 为 dataCode, 值为以类型分组的数据的查询结果
     */
    @Override
    public Map<String, Map<IntelliTypeEnum, IntelligentDataEntity>> queryByTypeWithLastTime(IntelliTypeEnum... types) {
        String time = this.lambdaQuery().select(IntelligentDataEntity::getTime).orderByDesc(IntelligentDataEntity::getTime).last("limit 1").one().getTime();
        List<IntelligentDataEntity> entities = this.lambdaQuery().in(ArrayUtils.isNotEmpty(types), IntelligentDataEntity::getIntelliType, Arrays.asList(types)).eq(IntelligentDataEntity::getTime, time).list();

        return entities.stream().collect(
                Collectors.groupingBy(IntelligentDataEntity::getDataCode, Collectors.collectingAndThen(
                                Collectors.toList(), list -> list.stream().collect(
                                        Collectors.toMap(IntelligentDataEntity::getIntelliType, o -> o, (v1, v2) -> v2)
                                )
                        )
                )
        );
    }

}
