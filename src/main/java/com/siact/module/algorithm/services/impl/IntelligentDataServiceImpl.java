package com.siact.module.algorithm.services.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.siact.common.config.KilnProperty;
import com.siact.common.exception.BizException;
import com.siact.common.redis.RedisService;
import com.siact.common.utils.JacksonUtils;
import com.siact.common.utils.TimeUtil;
import com.siact.module.algorithm.services.PythonAlgorithmService;
import com.siact.module.algorithm.socket.AlgorithmMessageWebSocket;
import com.siact.module.algorithm.constants.AlgorithmConstant;
import com.siact.module.algorithm.dto.IntelliTplSettingDTO;
import com.siact.module.algorithm.dto.IntelliTplSettingDetailDTO;
import com.siact.module.algorithm.entity.IncrementalLearnEntity;
import com.siact.module.algorithm.entity.IntelligentDataEntity;
import com.siact.module.algorithm.entity.TemperaturePredictEntity;
import com.siact.module.algorithm.enums.IntelliTypeEnum;
import com.siact.module.algorithm.mapper.IncrementalLearnMapper;
import com.siact.module.algorithm.mapper.IntelligentDataMapper;
import com.siact.module.algorithm.repository.TemperaturePredictRepository;
import com.siact.module.algorithm.services.AlgorithmService;
import com.siact.module.algorithm.services.IntelligentDataService;
import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.entity.KilnInfoEntity;
import com.siact.module.base.service.ControlIntervalConfigService;
import com.siact.module.base.service.IKilnInfoService;
import com.siact.module.device.entity.DeviceMappingEntity;
import com.siact.module.device.repository.DeviceMappingRepository;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private final PythonAlgorithmService pythonAlgorithmService;
    private final KilnProperty property;
    private final RedisService redis;
    private final SysConfigService sysConfigService;
    private final ControlIntervalConfigService configService;
    private final AlgorithmMessageWebSocket message;
    private final IncrementalLearnMapper incrementalLearnMapper;
    private final DeviceMappingRepository deviceMappingRepository;
    private final IKilnInfoService kilnInfoService;
    private final TemperaturePredictRepository temperaturePredictRepository;

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
        JSONObject params = buildBaseParams(SysConfigCodeConstants.INTELLI_COMPUTING_PARAMS);

        // 添加温度设定值参数
        SysConfigDTO controlTargetPoints = sysConfigService.getByCode(SysConfigCodeConstants.CONTROL_TARGET_POINTS);
        Map<String, Map<String, Object>> cptData = (Map<String, Map<String, Object>>) controlTargetPoints.getData();
        List<String> dataCodeList = cptData.values().stream()
                .map(v -> MapUtils.getString(v, "code"))
                .collect(Collectors.toList());
        List<ControlIntervalConfigDTO> targetTemps = configService.selectListByDataCodeList(dataCodeList);
        for (ControlIntervalConfigDTO dto : targetTemps) {
          params.put(dto.getMeasurePoint() + "_SP", new BigDecimal(dto.getTemperatureSet()));
        }

        // 构建温度限制参数 EK
        params.put("EK", buildEKParams(cptData, targetTemps));

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

        JSONObject result = response.getJSONObject("result");
        // JSONObject result = rs.getJSONObject("result1");
        // 获取模型训练结果
        // BigDecimal modelTransformer = result.getBigDecimal("gas_setValue_transformer");
        // BigDecimal modelDeltaC = result.getBigDecimal("gas_deltaC_transformer");
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
        // collect.add(builder.intelliType(IntelliTypeEnum.GAS_TRANSFORMER_MODEL).val(modelTransformer).build());
        // collect.add(builder.intelliType(IntelliTypeEnum.GAS_DELTAC_MODEL).val(modelDeltaC).build());
        collect.add(builder.intelliType(IntelliTypeEnum.GAS_DELTAC_EXPERT).val(expertDeltaC).build());
        collect.add(builder.intelliType(IntelliTypeEnum.GAS_LAST_SUM).val(lastGasSum).build());

        // 解析温度预测数据
        JSONObject temps = result.getJSONObject("temps");
        if (temps != null) {
            ArrayList<TemperaturePredictEntity> predictList = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> entry : cptData.entrySet()) {
                String pointName = entry.getKey();
                Map<String, Object> pointConfig = entry.getValue();
                String propName = MapUtils.getString(pointConfig, "name");
                String propCode = MapUtils.getString(pointConfig, "code");

                JSONObject tempData = temps.getJSONObject(propName);
                if (tempData == null) {
                    continue;
                }
                BigDecimal predValue = tempData.getBigDecimal("pred_value");
                if (predValue == null) {
                    continue;
                }

                Integer step = MapUtils.getInteger(pointConfig, "step");
                String predictTime = step != null ? TimeUtil.getCalcTime(time, step, "MIN") : time;

                predictList.add(TemperaturePredictEntity.builder()
                        .pointName(pointName)
                        .propName(propName)
                        .propCode(propCode)
                        .time(predictTime)
                        .itemValue(predValue)
                        .build());
            }
            if (!predictList.isEmpty()) {
                temperaturePredictRepository.saveBatch(predictList);
            }
        }

        // 校验约束规则并设置 ruleValid 字段
        validateGasCalcResult(collect);

        // 保存数据
        saveBatch(collect);

        // 对比数据, deltaC 变动则设置缓存, 调整 flag 参数
        if (!BigDecimal.ZERO.equals(expertDeltaC)) {
            // 设置 cache 四十分钟过期
            redis.setCacheObject(AlgorithmConstant.INTELLI_ALGORITHM_CACHE_KEY, lastGasSum.add(expertDeltaC), property.getAlgorithm().getIntelliStopInterval(), TimeUnit.MINUTES);
            message.intelliUpdate();
        }
    }

    /**
     * @Author: HouBo
     * @Date: 2026/5/11 10:18
     * @Description: 校验智控计算值是否在天然气流量设定值上下限范围内
     * 智控计算值 = last_gas_sum_sv + delta_C
     */
    private void validateGasCalcResult(ArrayList<IntelligentDataEntity> collect) {
        BigDecimal lastGasSum = null;
        BigDecimal deltaC = null;
        for (IntelligentDataEntity entity : collect) {
            if (IntelliTypeEnum.GAS_LAST_SUM.equals(entity.getIntelliType()) && entity.getVal() != null) {
                lastGasSum = entity.getVal();
            }
            if (IntelliTypeEnum.GAS_DELTAC_EXPERT.equals(entity.getIntelliType()) && entity.getVal() != null) {
                deltaC = entity.getVal();
            }
        }

        if (lastGasSum == null || deltaC == null) {
            log.warn("智控校验：未获取到 GAS_LAST_SUM 或 GAS_DELTAC_EXPERT 数据，跳过校验");
            return;
        }

        BigDecimal calcValue = lastGasSum.add(deltaC);

        KilnInfoEntity kilnInfo = kilnInfoService.getEnabledTotal();
        if (kilnInfo == null || kilnInfo.getGasValUp() == null || kilnInfo.getGasValLow() == null) {
            log.warn("智控校验：未查询到 kiln_info 总量记录或上下限未配置，跳过校验");
            return;
        }

        BigDecimal upperLimit = kilnInfo.getGasValUp();
        BigDecimal lowerLimit = kilnInfo.getGasValLow();
        boolean outOfRange = calcValue.compareTo(lowerLimit) < 0 || calcValue.compareTo(upperLimit) > 0;

        log.info("智控校验：计算值={}, 上限={}, 下限={}, 是否超限={}", calcValue, upperLimit, lowerLimit, outOfRange);
        collect.forEach(e -> e.setRuleValid(!outOfRange));
    }

    @SuppressWarnings("unchecked")
    private JSONObject buildBaseParams(String configCode) {
        LocalDateTime now = LocalDateTime.now().withSecond(0);

        SysConfigDTO configDTO = sysConfigService.getByCode(configCode);
        Map<String, Object> cfgData = (Map<String, Object>) configDTO.getData();

        JSONObject params = new JSONObject();
        params.put("ts", MapUtils.getString(cfgData, "ts"));
        params.put("startTime", now.plusMinutes(-MapUtils.getInteger(cfgData, "tracingTime")).format(TimeUtil.df));
        params.put("endTime", now.format(TimeUtil.df));

        Map<String, String> data = new HashMap<>();
        List<Map<String, String>> keyData = (List<Map<String, String>>) cfgData.get("keyData");
        keyData.forEach(item -> data.put(MapUtils.getString(item, "algorithmCode"), MapUtils.getString(item, "dataCode")));
        params.put("data", data);

        return params;
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

    private Map<String, Map<String, BigDecimal>> buildEKParams(Map<String, Map<String, Object>> cptData, List<ControlIntervalConfigDTO> targetTemps) {
        // 按 measurePoint 建立 targetTemps 索引
        Map<String, ControlIntervalConfigDTO> tempMap = new HashMap<>();
        for (ControlIntervalConfigDTO dto : targetTemps) {
            tempMap.put(dto.getMeasurePoint(), dto);
        }

        Map<String, Map<String, BigDecimal>> ek = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : cptData.entrySet()) {
            String pointName = entry.getKey();
            Map<String, Object> pointConfig = entry.getValue();

            BigDecimal ekl = new BigDecimal(MapUtils.getString(pointConfig, "ekl"));
            ControlIntervalConfigDTO tempConfig = tempMap.get(pointName);
            if (tempConfig == null) {
                log.warn("EK参数构建: 未找到点位 {} 对应的温度控制配置", pointName);
                continue;
            }

            BigDecimal temperatureSet = new BigDecimal(tempConfig.getTemperatureSet());
            Map<String, BigDecimal> limitMap = new LinkedHashMap<>();
            limitMap.put("EKL", ekl);
            limitMap.put("EKH", temperatureSet.subtract(new BigDecimal(tempConfig.getLowControl())).abs());
            limitMap.put("EKS", temperatureSet.subtract(new BigDecimal(tempConfig.getLowAlarm())).abs());
            ek.put(pointName, limitMap);
        }
        return ek;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void callSelfLearningAlgorithm() {
        log.info("===== 自学习算法调用开始 =====");

        // 1. 读取点位配置
        SysConfigDTO config = sysConfigService.getByCode(SysConfigCodeConstants.SELF_LEARNING_DATACODE);
        List<Map<String, String>> points = (List<Map<String, String>>) config.getData();

        if (points == null || points.isEmpty()) {
            log.error("自学习算法配置中没有点位数据");
            return;
        }

        // 2. 构建 name → dataCode 映射
        Map<String, String> data = new LinkedHashMap<>();
        for (Map<String, String> point : points) {
            data.put(point.get("name"), point.get("dataCode"));
        }

        // 3. 调用 Python 算法
        Map<String, String> params = new LinkedHashMap<>();
        params.put("data", JacksonUtils.toJson(data));

        Map<String, Object> result = pythonAlgorithmService.execute("incremental_finetune.py", params, new TypeReference<Map<String, Object>>() {});
        log.info("自学习算法调用成功, 返回结果: {}", result);
        log.info("===== 自学习算法调用结束 =====");
    }

    @Override
    public void callIncrementalLearn() {
        log.info("===== 增量学习算法调用开始 =====");

        // 1. 读取参数配置
        JSONObject params = buildBaseParams(SysConfigCodeConstants.INCREMENTAL_LEARN_PARAMS);

        // 2. 调用算法接口
        JSONObject response;
        try {
            response = algorithmService.callResolve(
                    "incremental_learn",
                    JSONObject.toJSONString(params),
                    () -> HttpUtil.post(property.getAlgorithm().getBaseUrl() + "/incremental_learn", params.toJSONString(), property.getAlgorithm().getIntelligentTimeout())
            );
        } catch (BizException e) {
            log.error("增量学习算法调用异常: {}", e.getMessage());
            return;
        }

        // 3. 解析结果
        JSONObject result = response.getJSONObject("result");
        JSONObject trainInfos = result.getJSONObject("train_infos");
        String targetColumn = trainInfos.getString("target_column");
        String savePath = trainInfos.getString("save_path");

        JSONObject valMetrics = trainInfos.getJSONObject("val_metrics");
        JSONObject testMetrics = trainInfos.getJSONObject("test_metrics");

        // 4. 通过 target_column 查询 device_mapping 获取 data_code
        String dataCode = null;
        DeviceMappingEntity mapping = deviceMappingRepository.findByPropName(targetColumn);
        if (mapping != null) {
            dataCode = mapping.getPropCode();
        } else {
            log.warn("未在 device_mapping 中找到 target_column={} 对应的记录", targetColumn);
        }

        // 5. 保存到 incremental_learn 表
        IncrementalLearnEntity entity = IncrementalLearnEntity.builder()
                .dataCode(dataCode)
                .targetName(targetColumn)
                .modelPath(savePath)
                .valLoss(valMetrics.getBigDecimal("loss"))
                .valMae(valMetrics.getBigDecimal("mae"))
                .valRmse(valMetrics.getBigDecimal("rmse"))
                .valR2(valMetrics.getBigDecimal("r2"))
                .testLoss(testMetrics.getBigDecimal("loss"))
                .testMae(testMetrics.getBigDecimal("mae"))
                .testRmse(testMetrics.getBigDecimal("rmse"))
                .testR2(testMetrics.getBigDecimal("r2"))
                .validity(false)
                .remark(JacksonUtils.toJson(response))
                .build();
        incrementalLearnMapper.insert(entity);

        log.info("增量学习结果已保存, target={}, model={}", targetColumn, savePath);
        log.info("===== 增量学习算法调用结束 =====");
    }
}
