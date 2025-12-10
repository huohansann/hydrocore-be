package com.siact.module.snapshot.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.siact.common.constant.ConstantBase;
import com.siact.common.constant.ConstantSymbol;
import com.siact.common.utils.JacksonUtils;
import com.siact.common.utils.MapUtils;
import com.siact.common.utils.TimeUtil;
import com.siact.module.algorithm.entity.IntelligentDataEntity;
import com.siact.module.algorithm.enums.IntelliTypeEnum;
import com.siact.module.algorithm.services.IntelligentDataService;
import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.service.ControlIntervalConfigService;
import com.siact.module.base.service.TplService;
import com.siact.module.control.entity.ControlSettingGasEntity;
import com.siact.module.control.service.ControlSettingGasService;
import com.siact.module.predicted.entity.PredictedDataEntity;
import com.siact.module.predicted.enums.PredictedTypeEnum;
import com.siact.module.predicted.service.PredictedDataService;
import com.siact.module.snapshot.dto.SnapshotChartQueryDTO;
import com.siact.module.snapshot.dto.SnapshotChartQueryDetailDTO;
import com.siact.module.snapshot.dto.SnapshotTplSettingDTO;
import com.siact.module.snapshot.dto.SnapshotTplSettingDetailDTO;
import com.siact.module.snapshot.entity.SnapshotGasEntity;
import com.siact.module.snapshot.entity.SnapshotTempEntity;
import com.siact.module.snapshot.service.SnapshotGasService;
import com.siact.module.snapshot.service.SnapshotPublicService;
import com.siact.module.snapshot.service.SnapshotTempService;
import com.siact.module.snapshot.vo.SnapshotChartDetailVO;
import com.siact.module.snapshot.vo.SnapshotChartVO;
import com.siact.sec.sevice.DataService;
import com.siact.sec.utils.IntervalTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 快照服务实现类
 *
 * @author Roo
 * @date 2025-09-22
 */
@Slf4j
@Service
public class SnapshotPublicServiceImpl implements SnapshotPublicService {
    private @Resource TplService tplService;
    private @Resource DataService dataService;
    private @Resource PredictedDataService predictedDataService;
    private @Resource ControlIntervalConfigService controlIntervalConfigService;
    private @Resource ControlSettingGasService controlSettingGasService;
    private @Resource SnapshotTempService snapshotTempService;
    private @Resource SnapshotGasService snapshotGasService;
    private @Resource IntelligentDataService intelligentDataService;

    @Override
    public SnapshotChartVO queryChart(SnapshotChartQueryDTO queryDTO) {

        SnapshotChartVO chartVO = new SnapshotChartVO();

        List<String> timeList = IntervalTimeUtil.getIntervalTimeList(queryDTO.getStartTime(), queryDTO.getEndTime(), queryDTO.getTsUnit(), queryDTO.getTs(), queryDTO.getFormatVal());

        chartVO.setXAxis(timeList);

        List<SnapshotChartQueryDetailDTO> queryList = queryDTO.getQueryList();
        if (queryList == null) {
            return chartVO;
        }

        // 表单返回数据
        ArrayList<SnapshotChartDetailVO> chartData = new ArrayList<>();

        Map<String, List<String>> typeDataCodeMap = queryList.stream()
                .collect(Collectors.groupingBy(SnapshotChartQueryDetailDTO::getType,
                        Collectors.collectingAndThen(Collectors.toList(), list -> list.stream().map(SnapshotChartQueryDetailDTO::getDataCodeList).filter(ObjectUtils::isNotEmpty)
                                .flatMap(List::stream).distinct().collect(Collectors.toList()))));


        List<String> tempDataCodeList = typeDataCodeMap.get("TEMP");
        // 查询temp温度类型数据
        List<SnapshotTempEntity> tempSnapshotList = new ArrayList<>();
        if (tempDataCodeList != null && !tempDataCodeList.isEmpty()) {
            tempSnapshotList =
                    snapshotTempService.queryByDataCodeInRange(tempDataCodeList, queryDTO.getStartTime(), queryDTO.getEndTime());
        }

        // 温度数据map key: dataCode_createTime value: SnapshotTempEntity
        Map<String, SnapshotTempEntity> tempSnapshotCodeValMap =
                tempSnapshotList.stream().collect(Collectors.toMap(o -> o.getDataCode() + ConstantSymbol.UNDER_LINE + IntervalTimeUtil.dateFormat(o.getCreateTime(), queryDTO.getFormatVal()), o -> o, (o1, o2) -> o1));

        // 查询gas类型数据
        List<String> gasDataCodeList = typeDataCodeMap.get("GAS");
        // 查询gas类型数据
        List<SnapshotGasEntity> gasSnapshotList = new ArrayList<>();
        if (gasDataCodeList != null && !gasDataCodeList.isEmpty()) {
            gasSnapshotList =
                    snapshotGasService.queryByDataCodeInRange(gasDataCodeList, queryDTO.getStartTime(), queryDTO.getEndTime());
        }
        // gas数据map key: dataCode_createTime value: SnapshotGasEntity
        Map<String, SnapshotGasEntity> gasSnapshotCodeValMap = gasSnapshotList.stream()
                .collect(Collectors.toMap(o -> o.getDataCode() + ConstantSymbol.UNDER_LINE + IntervalTimeUtil.dateFormat(o.getCreateTime(), queryDTO.getFormatVal()), o -> o, (o1, o2) -> o1));

        // 处理返回数据
        BigDecimal tempMinVal = null;
        BigDecimal tempMaxVal = null;

        BigDecimal gasMinVal = null;
        BigDecimal gasMaxVal = null;

        for (SnapshotChartQueryDetailDTO queryDetailDTO : queryList) {
            // 查找的数据类型
            String code = queryDetailDTO.getCode();
            String type = queryDetailDTO.getType();

            List<String> dataCodeList = queryDetailDTO.getDataCodeList();

            ListIterator<String> dataCodeIterator = dataCodeList.listIterator();

            while (dataCodeIterator.hasNext()) {
                List<Object[]> curSnapshotTimeValData = new ArrayList<>();
                int curIndex = dataCodeIterator.nextIndex();
                String dataCode = dataCodeIterator.next();
                SnapshotChartDetailVO curSnapshotVo = new SnapshotChartDetailVO();
                curSnapshotVo.setType(queryDetailDTO.getType());
                curSnapshotVo.setCode(queryDetailDTO.getCode());
                List<String> nameList = queryDetailDTO.getNameList();
                if (nameList != null && !nameList.isEmpty()) {
                    curSnapshotVo.setName(nameList.get(curIndex));
                }

                curSnapshotVo.setDataCode(dataCode);

                for (String time : timeList) {
                    // map key: dataCode_createTime value: SnapshotTempEntity
                    String key = dataCode + ConstantSymbol.UNDER_LINE + time;
                    if ("TEMP".equals(type)) {
                        // 温度类型数据
                        SnapshotTempEntity tempSnapshot = tempSnapshotCodeValMap.get(key);
                        String curVal = calcChartTempVal(tempSnapshot, code);
                        Object[] curTimeVal = new Object[]{time, curVal};
                        curSnapshotTimeValData.add(curTimeVal);
                        if (ObjectUtils.isEmpty(curVal)) {
                            continue;
                        }
                        // 温度数据范围
                        if (tempMinVal == null || new BigDecimal(curVal).compareTo(tempMinVal) < 0) {
                            tempMinVal = new BigDecimal(curVal);
                        }
                        if (tempMaxVal == null || new BigDecimal(curVal).compareTo(tempMaxVal) > 0) {
                            tempMaxVal = new BigDecimal(curVal);
                        }
                    } else if ("GAS".equals(type)) {
                        // gas类型数据
                        SnapshotGasEntity gasSnapshot = gasSnapshotCodeValMap.get(key);
                        String curVal = calcChartGasVal(gasSnapshot, code);
                        Object[] curTimeVal = new Object[]{time, curVal};
                        curSnapshotTimeValData.add(curTimeVal);
                        if (ObjectUtils.isEmpty(curVal)) {
                            continue;
                        }
                        // gas数据范围
                        if (gasMinVal == null || new BigDecimal(curVal).compareTo(gasMinVal) < 0) {
                            gasMinVal = new BigDecimal(curVal);
                        }
                        if (gasMaxVal == null || new BigDecimal(curVal).compareTo(gasMaxVal) > 0) {
                            gasMaxVal = new BigDecimal(curVal);
                        }
                    }
                }
                curSnapshotVo.setData(curSnapshotTimeValData);
                chartData.add(curSnapshotVo);
            }
        }

        // 数据范围
        Map<String, List<BigDecimal>> rangeData = new HashMap<>();
        rangeData.put("TEMP", Arrays.asList(tempMinVal, tempMaxVal));

        rangeData.put("GAS", Arrays.asList(gasMinVal, gasMaxVal));

        chartVO.setRangeData(rangeData);
        chartVO.setChartData(chartData);

//        initTestChartData(queryList, timeList, chartVO);

        return chartVO;
    }

    private static String calcChartTempVal(SnapshotTempEntity tempSnapshot, String code) {
        String curVal = null;
        if (tempSnapshot != null) {
            if ("actualVal".equals(code)) {
                // 实际运行值
                BigDecimal val = tempSnapshot.getActualVal();
                curVal = ObjectUtils.isEmpty(val) ? null : val.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            } else if ("singlePredictedT20Val".equals(code)) {
                // 单点预测值(T20)
                BigDecimal val = tempSnapshot.getSinglePredictedT20Val();
                curVal = ObjectUtils.isEmpty(val) ? null : val.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            } else if ("singlePredictedT40Val".equals(code)) {
                // 单点预测值(T40)
                BigDecimal val = tempSnapshot.getSinglePredictedT40Val();
                curVal = ObjectUtils.isEmpty(val) ? null : val.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            } else if ("singlePredictedT60Val".equals(code)) {
                // 单点预测值(T60)
                BigDecimal val = tempSnapshot.getSinglePredictedT60Val();
                curVal = ObjectUtils.isEmpty(val) ? null : val.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            } else if ("singlePredictedT80Val".equals(code)) {
                // 单点预测值(T80)
                BigDecimal val = tempSnapshot.getSinglePredictedT80Val();
                curVal = ObjectUtils.isEmpty(val) ? null : val.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            } else if ("singlePredictedT27Val".equals(code)) {
                // 单点预测值(T27)
                BigDecimal val = tempSnapshot.getSinglePredictedT27Val();
                curVal = ObjectUtils.isEmpty(val) ? null : val.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            } else if ("singlePredictedT54Val".equals(code)) {
                // 单点预测值(T54)
                BigDecimal val = tempSnapshot.getSinglePredictedT54Val();
                curVal = ObjectUtils.isEmpty(val) ? null : val.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            } else if ("multiPredictedVal".equals(code)) {
                // 多步预测值
                BigDecimal val = tempSnapshot.getMultiPredictedVal();
                curVal = ObjectUtils.isEmpty(val) ? null : val.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            } else if ("tempSetVal".equals(code)) {
                // 温度设定值(取控制设置-控制区间设置-温度设定值)
                curVal = tempSnapshot.getTempSetVal();
            } else if ("predictedMaxVal".equals(code) && ObjectUtils.isNotEmpty(tempSnapshot.getPredictedMaxVal())) {
                // 预测最大值
                curVal = tempSnapshot.getPredictedMaxVal().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            } else if ("predictedMinVal".equals(code) && ObjectUtils.isNotEmpty(tempSnapshot.getPredictedMinVal())) {
                // 预测最小值
                curVal = tempSnapshot.getPredictedMinVal().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            }

        }
        return curVal;
    }

    private static String calcChartGasVal(SnapshotGasEntity gasSnapshot, String code) {
        String curVal = null;
        if (gasSnapshot != null) {
            if ("gasDcsVal".equals(code)) {
                // 天然气 DCS 值
                BigDecimal val = gasSnapshot.getGasDcsVal();
                curVal = ObjectUtils.isEmpty(val) ? null : val.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
                // } else if ("gasAlgorithmCalcVal".equals(code)) {
            } else if (StringUtils.startsWith(code, "gasAlgorithmCalcVal")) {
                @SuppressWarnings("unchecked") Map<String, BigDecimal> map = JacksonUtils.fromJson(StringUtils.defaultIfEmpty(gasSnapshot.getAlgorithmCalcVal(), "{}"), Map.class);
                curVal = map.getOrDefault(code, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            } else if ("gasManualVal".equals(code)) {
                // 天然气人工值
                BigDecimal val = gasSnapshot.getGasManualVal();
                curVal = ObjectUtils.isEmpty(val) ? null : val.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            }
        }
        return curVal;
    }

    @Override
    public void execSnapshotTask(String nowTime) {

        // 如果当前时间有数据,先删除
        snapshotTempService.remove(new LambdaQueryWrapper<SnapshotTempEntity>().eq(SnapshotTempEntity::getCreateTime, nowTime));
        snapshotGasService.remove(new LambdaQueryWrapper<SnapshotGasEntity>().eq(SnapshotGasEntity::getCreateTime, nowTime));

        // 查询快照tpl模板配置
        List<SnapshotTplSettingDTO> snapshotQuery = tplService.getListByCode("snapshotQuery", SnapshotTplSettingDTO.class);
        if (snapshotQuery == null || snapshotQuery.isEmpty()) {
            log.error("snapshotQuery没有tpl配置");
            return;
        }

        Map<String, List<SnapshotTplSettingDTO>> snapCodeSettingTypeMap = snapshotQuery.stream()
                .collect(Collectors.groupingBy(SnapshotTplSettingDTO::getType));

        // 根据snapshotQuery初始化temp温度类型的实体数据 k:dataCode v:SnapshotTempEntity
        Map<String, SnapshotTempEntity> tempEntityMap = initSnapshotTempEntity(snapCodeSettingTypeMap.get("TEMP"), nowTime);

        // 根据snapshotQuery初始化gas天然气类型的实体数据 k:dataCode v:SnapshotGasEntity
        Map<String, SnapshotGasEntity> gasEntityMap = initSnapshotGasEntity(snapCodeSettingTypeMap.get("GAS"), nowTime);

        // 处理快照数据
        Map<String, SnapshotTplSettingDTO> snapCodeSettingCodeMap = snapshotQuery.stream().collect(Collectors.toMap(SnapshotTplSettingDTO::getCode, o -> o, (oldVal, newVal) -> oldVal));
        handleSnapshotData(nowTime, snapCodeSettingCodeMap, tempEntityMap, gasEntityMap);

        // 保存 tempEntityMap
        snapshotTempService.saveBatch(tempEntityMap.values());

        // 保存 gasEntityMap
        snapshotGasService.saveBatch(gasEntityMap.values());

    }

    @Override
    public void clearSnapshotTask(String halfYearAgoTime) {
        // 删除半年前的快照数据
        snapshotTempService.remove(new LambdaQueryWrapper<SnapshotTempEntity>().lt(SnapshotTempEntity::getCreateTime, halfYearAgoTime));
        snapshotGasService.remove(new LambdaQueryWrapper<SnapshotGasEntity>().lt(SnapshotGasEntity::getCreateTime, halfYearAgoTime));
    }

    /**
     * 处理快照数据
     */
    private void handleSnapshotData(String nowTime, Map<String, SnapshotTplSettingDTO> snapCodeSettingMap, Map<String, SnapshotTempEntity> tempEntityMap, Map<String, SnapshotGasEntity> gasEntityMap) {
        // 数据分为
        // 1:实际运行值 天然气DCS值  查询孪生
        handleActualValAndGasDecVal(nowTime, snapCodeSettingMap, tempEntityMap, gasEntityMap);

        // 2:单点预测值 多点预测值  查询库  predicted_data
        handlePredictedData(nowTime, tempEntityMap);

        // 3:温度设定值  查询库  control_interval_config  code tempSetVal
        handleTempSetVal(snapCodeSettingMap, tempEntityMap);

        // 4:预测最大值和预测最小值
        handlePredicateExtremum(snapCodeSettingMap, tempEntityMap);
        // 5:天然气智控
        handleGasIntelliVal(snapCodeSettingMap, gasEntityMap);
        // 6:天然气人工值  查询库  control_setting_gas
        handleGasManualVal(snapCodeSettingMap, gasEntityMap);
    }

    private void handleGasIntelliVal(Map<String, SnapshotTplSettingDTO> snapCodeSettingMap, Map<String, SnapshotGasEntity> gasEntityMap) {
        SnapshotTplSettingDTO gasAlgorithmCalcVal = snapCodeSettingMap.get("gasAlgorithmCalcVal");
        if (ObjectUtils.isEmpty(gasAlgorithmCalcVal) || CollectionUtils.isEmpty(gasAlgorithmCalcVal.getQueryDataCode())) {
            log.error("gasAlgorithmCalcVal 没有 tpl 配置 dataCode, 无法执行快照任务");
            return;
        }
        // 点位编码
        List<String> dataCodes = gasAlgorithmCalcVal.getQueryDataCode().stream().map(SnapshotTplSettingDetailDTO::getDataCode).distinct().collect(Collectors.toList());

        // 获取数据
        Map<String, Map<IntelliTypeEnum, IntelligentDataEntity>> intelliValues = intelligentDataService.queryByTypeWithLastTime(
                IntelliTypeEnum.GAS_CALC_MODEL1,
                IntelliTypeEnum.GAS_CALC_MODEL2,
                IntelliTypeEnum.GAS_CALC_EXPERT1,
                IntelliTypeEnum.GAS_CALC_EXPERT2,
                IntelliTypeEnum.GAS_RUN_VALUE
        );

        for (String dataCode : dataCodes) {
            SnapshotGasEntity snapshotGasEntity = gasEntityMap.get(dataCode);
            Map<IntelliTypeEnum, IntelligentDataEntity> intelliMap = intelliValues.get(dataCode);
            if (!Objects.isNull(snapshotGasEntity)) {
                IntelligentDataEntity runValue = intelliMap.get(IntelliTypeEnum.GAS_RUN_VALUE);
                IntelligentDataEntity model1 = intelliMap.get(IntelliTypeEnum.GAS_CALC_MODEL1);
                IntelligentDataEntity model2 = intelliMap.get(IntelliTypeEnum.GAS_CALC_MODEL2);
                IntelligentDataEntity expert1 = intelliMap.get(IntelliTypeEnum.GAS_CALC_EXPERT1);
                IntelligentDataEntity expert2 = intelliMap.get(IntelliTypeEnum.GAS_CALC_EXPERT2);

                // 基于 Model method1
                BigDecimal modelValue1 = runValue.getVal().add(model1.getVal());
                BigDecimal modelValue2 = runValue.getVal().add(model2.getVal());
                // 基于专家经验
                BigDecimal experienceValue1 = runValue.getVal().add(expert1.getVal());
                BigDecimal experienceValue2 = runValue.getVal().add(expert2.getVal());

                Map<String, BigDecimal> algorithmCalcVal = MapUtils.of("gasAlgorithmCalcValM1", modelValue1, "gasAlgorithmCalcValM2", modelValue2, "gasAlgorithmCalcValE1", experienceValue1, "gasAlgorithmCalcValE2", experienceValue2);
                snapshotGasEntity.setGasAlgorithmCalcVal(modelValue2);
                snapshotGasEntity.setAlgorithmCalcVal(JacksonUtils.toJson(algorithmCalcVal));
            }
        }

    }

    /*  处理预测温度最大最小值 */
    private void handlePredicateExtremum(Map<String, SnapshotTplSettingDTO> snapCodeSettingMap, Map<String, SnapshotTempEntity> tempEntityMap) {
        SnapshotTplSettingDTO gasAlgorithmCalcVal = snapCodeSettingMap.get("predictedMaxVal");
        if (ObjectUtils.isEmpty(gasAlgorithmCalcVal) || CollectionUtils.isEmpty(gasAlgorithmCalcVal.getQueryDataCode())) {
            log.error("predictedMinVal 没有 tpl 配置 dataCode, 无法执行快照任务");
            return;
        }
        // 点位编码
        List<String> dataCodes = gasAlgorithmCalcVal.getQueryDataCode().stream().map(SnapshotTplSettingDetailDTO::getDataCode).distinct().collect(Collectors.toList());

        // 获取数据
        Map<String, Map<IntelliTypeEnum, IntelligentDataEntity>> intelliValues = intelligentDataService.queryByTypeWithLastTime(IntelliTypeEnum.MIN_TEMP, IntelliTypeEnum.MAX_TEMP);

        for (String dataCode : dataCodes) {
            SnapshotTempEntity snapshotTempEntity = tempEntityMap.get(dataCode);
            Map<IntelliTypeEnum, IntelligentDataEntity> intelliMap = intelliValues.get(dataCode);
            if (!Objects.isNull(snapshotTempEntity)) {
                IntelligentDataEntity minValue = intelliMap.get(IntelliTypeEnum.MIN_TEMP);
                IntelligentDataEntity maxValue = intelliMap.get(IntelliTypeEnum.MAX_TEMP);
                snapshotTempEntity.setPredictedMinVal(minValue.getVal());
                snapshotTempEntity.setPredictedMaxVal(maxValue.getVal());
            }
        }
    }

    /**
     * 处理天然气人工值
     */
    private void handleGasManualVal(Map<String, SnapshotTplSettingDTO> snapCodeSettingMap, Map<String, SnapshotGasEntity> gasEntityMap) {
        SnapshotTplSettingDTO gasManualValSetting = snapCodeSettingMap.get("gasManualVal");

        if (gasManualValSetting != null && gasManualValSetting.getQueryDataCode() != null) {
            List<String> gasManualValDataCodeList
                    = gasManualValSetting.getQueryDataCode().stream()
                    .map(SnapshotTplSettingDetailDTO::getDataCode)
                    .distinct()
                    .collect(Collectors.toList());
            List<ControlSettingGasEntity> gasControlSetting = controlSettingGasService.getValidList();

            for (ControlSettingGasEntity controlSettingGasEntity : gasControlSetting) {
                SnapshotGasEntity snapshotGasEntity = gasEntityMap.get(controlSettingGasEntity.getDataCode());
                if (snapshotGasEntity != null && gasManualValDataCodeList.contains(snapshotGasEntity.getDataCode())) {
                    snapshotGasEntity.setGasManualVal(controlSettingGasEntity.getGasManualVal());
                }
            }

        } else {
            log.error("gasManualVal没有tpl配置dataCode,无法执行快照任务");
        }
    }

    /**
     * 处理温度设定值
     */
    private void handleTempSetVal(Map<String, SnapshotTplSettingDTO> snapCodeSettingMap, Map<String, SnapshotTempEntity> tempEntityMap) {
        SnapshotTplSettingDTO tempSetValSetting = snapCodeSettingMap.get("tempSetVal");

        if (tempSetValSetting != null && tempSetValSetting.getQueryDataCode() != null) {
            List<String> tempSetValDataCodeList
                    = tempSetValSetting.getQueryDataCode().stream()
                    .map(SnapshotTplSettingDetailDTO::getDataCode)
                    .distinct()
                    .collect(Collectors.toList());
            List<ControlIntervalConfigDTO> controlIntervalConfigList = controlIntervalConfigService.selectListByDataCodeList(tempSetValDataCodeList);
            for (ControlIntervalConfigDTO configDTO : controlIntervalConfigList) {
                SnapshotTempEntity snapshotTempEntity = tempEntityMap.get(configDTO.getDataCode());
                if (snapshotTempEntity != null) {
                    snapshotTempEntity.setTempSetVal(configDTO.getTemperatureSet());
                }
            }
        } else {
            log.error("tempSetVal没有tpl配置dataCode,无法执行快照任务");
        }

    }

    /**
     * 处理步长预测数据(根据时间查询预测数据)
     * T20 T40 T60 T80 T27 T54 多步
     *
     * @param nowTime
     * @param tempEntityMap
     */
    private void handlePredictedData(String nowTime, Map<String, SnapshotTempEntity> tempEntityMap) {
        List<PredictedDataEntity> predictedDataList = predictedDataService.queryDataByTime(null, null, nowTime);

        if (predictedDataList != null && !predictedDataList.isEmpty()) {
            for (PredictedDataEntity predictedDataEntity : predictedDataList) {
                SnapshotTempEntity snapshotTempEntity = tempEntityMap.get(predictedDataEntity.getDataCode());
                if (snapshotTempEntity != null) {

                    String predictedTypeCode = predictedDataEntity.getPredictedTypeCode();

                    if (PredictedTypeEnum.SINGLE_T20.getCode().equals(predictedTypeCode)) {
                        snapshotTempEntity.setSinglePredictedT20Val(predictedDataEntity.getItemVal());
                    } else if (PredictedTypeEnum.SINGLE_T40.getCode().equals(predictedTypeCode)) {
                        snapshotTempEntity.setSinglePredictedT40Val(predictedDataEntity.getItemVal());
                    } else if (PredictedTypeEnum.SINGLE_T60.getCode().equals(predictedTypeCode)) {
                        snapshotTempEntity.setSinglePredictedT60Val(predictedDataEntity.getItemVal());
                    } else if (PredictedTypeEnum.SINGLE_T80.getCode().equals(predictedTypeCode)) {
                        snapshotTempEntity.setSinglePredictedT80Val(predictedDataEntity.getItemVal());
                    } else if (PredictedTypeEnum.SINGLE_T27.getCode().equals(predictedTypeCode)) {
                        snapshotTempEntity.setSinglePredictedT27Val(predictedDataEntity.getItemVal());
                    } else if (PredictedTypeEnum.SINGLE_T54.getCode().equals(predictedTypeCode)) {
                        snapshotTempEntity.setSinglePredictedT54Val(predictedDataEntity.getItemVal());
                    } else if (PredictedTypeEnum.MULTI.getCode().equals(predictedTypeCode)) {
                        snapshotTempEntity.setMultiPredictedVal(predictedDataEntity.getItemVal());
                    }
                }
            }
        }
    }


    /**
     * 初始化 temp 温度类型的实体数据
     */
    private Map<String, SnapshotTempEntity> initSnapshotTempEntity(List<SnapshotTplSettingDTO> tempSetValList, String nowTime) {
        // 根据snapshotQuery初始化temp温度类型的实体数据 k:dataCode v:SnapshotTempEntity
        Map<String, SnapshotTempEntity> tempEntityMap = new HashMap<>();

        for (SnapshotTplSettingDTO tempSetVal : tempSetValList) {
            if (tempSetVal != null && tempSetVal.getQueryDataCode() != null) {
                tempSetVal.getQueryDataCode().forEach(queryDetailDTO -> {
                    SnapshotTempEntity snapshotTempEntity = new SnapshotTempEntity();
                    snapshotTempEntity.setDataCode(queryDetailDTO.getDataCode());
                    snapshotTempEntity.setName(queryDetailDTO.getName());
                    snapshotTempEntity.setCreateTime(nowTime);
                    tempEntityMap.put(queryDetailDTO.getDataCode(), snapshotTempEntity);
                });
            }
        }


        return tempEntityMap;
    }

    /**
     * 初始化gas天然气类型的实体数据
     */
    private Map<String, SnapshotGasEntity> initSnapshotGasEntity(List<SnapshotTplSettingDTO> gasSetValList, String nowTime) {
        // 根据snapshotQuery初始化gas天然气类型的实体数据 k:dataCode v:SnapshotGasEntity
        Map<String, SnapshotGasEntity> gasEntityMap = new HashMap<>();
        for (SnapshotTplSettingDTO gasSetVal : gasSetValList) {
            if (gasSetVal != null && gasSetVal.getQueryDataCode() != null) {
                gasSetVal.getQueryDataCode().forEach(queryDetailDTO -> {
                    SnapshotGasEntity snapshotGasEntity = new SnapshotGasEntity();
                    snapshotGasEntity.setDataCode(queryDetailDTO.getDataCode());
                    snapshotGasEntity.setName(queryDetailDTO.getName());
                    snapshotGasEntity.setCreateTime(nowTime);
                    gasEntityMap.put(queryDetailDTO.getDataCode(), snapshotGasEntity);
                });
            }
        }
        return gasEntityMap;
    }

    /**
     * 处理实际运行值和天然气DCS值
     *
     * @param nowTime
     * @param snapCodeSettingMap
     * @param tempEntityMap
     * @param gasEntityMap
     */
    private void handleActualValAndGasDecVal(String nowTime,
                                             Map<String, SnapshotTplSettingDTO> snapCodeSettingMap,
                                             Map<String, SnapshotTempEntity> tempEntityMap,
                                             Map<String, SnapshotGasEntity> gasEntityMap) {
        // 1.1 获取实际运行值的dataCode
        SnapshotTplSettingDTO actualValSetting = snapCodeSettingMap.get("actualVal");

        Map<String, SnapshotTplSettingDetailDTO> actualValDataCodeMap = new HashMap<>();
        if (actualValSetting != null && actualValSetting.getQueryDataCode() != null) {
            actualValDataCodeMap
                    = actualValSetting.getQueryDataCode().stream()
                    .collect(Collectors.toMap(SnapshotTplSettingDetailDTO::getDataCode, o -> o, (oldVal, newVal) -> oldVal));
        } else {
            log.error("actualVal没有tpl配置dataCode,无法执行快照任务");
        }
        List<String> actualValDataCodeList = new ArrayList<>(actualValDataCodeMap.keySet());

        // 1.2 获取天然气DCS值 gasDcsVal
        SnapshotTplSettingDTO gasDcsValSetting = snapCodeSettingMap.get("gasDcsVal");
        Map<String, SnapshotTplSettingDetailDTO> gasDcsValDataCodeMap = new HashMap<>();
        if (gasDcsValSetting != null && gasDcsValSetting.getQueryDataCode() != null) {
            gasDcsValDataCodeMap
                    = gasDcsValSetting.getQueryDataCode().stream()
                    .collect(Collectors.toMap(SnapshotTplSettingDetailDTO::getPropCode, o -> o, (oldVal, newVal) -> oldVal));
        } else {
            log.error("gasDcsVal没有tpl配置dataCode,无法执行快照任务");
        }
        List<String> gasDcsValDataCodeList = new ArrayList<>(gasDcsValDataCodeMap.keySet());

        // 1.3 查询数字孪生
        List<String> allDataCodeList = new ArrayList<>();
        allDataCodeList.addAll(actualValDataCodeList);
        allDataCodeList.addAll(gasDcsValDataCodeList);
        // 开始时间为当前nowTime 向前提一分钟
        String startTime = TimeUtil.getCalcTime(nowTime, -1, "MIN");
        JSONObject secDataCodeValJsonObj = dataService.queryBetweenVal(
                String.join(ConstantSymbol.COMMA, allDataCodeList),
                startTime,
                nowTime,
                ConstantBase.LAST);

        if (secDataCodeValJsonObj == null) {
            log.error("queryBetweenVal查询数据为空,dataCodeList:{}", allDataCodeList);
            return;
        }

        // 处理窑炉温度相关
        // actualValDataCodeMap k:dataCode v:SnapshotTplSettingDetailDTO
        for (Map.Entry<String, SnapshotTplSettingDetailDTO> settingEntry : actualValDataCodeMap.entrySet()) {
            // 处理温度-实际值
            String dataCode = settingEntry.getKey();
            SnapshotTplSettingDetailDTO settingDetailDTO = settingEntry.getValue();
            SnapshotTempEntity snapshotTempEntity = tempEntityMap.get(settingDetailDTO.getDataCode());
            if (snapshotTempEntity != null) {
                // 数字孪生获取数据
                BigDecimal dataVal = secDataCodeValJsonObj.getBigDecimal(dataCode);
                snapshotTempEntity.setActualVal(dataVal);
            }
        }
        // 处理天然气相关
        // gasDcsValDataCodeMap k:dataCode v:SnapshotTplSettingDetailDTO
        for (Map.Entry<String, SnapshotTplSettingDetailDTO> settingEntry : gasDcsValDataCodeMap.entrySet()) {
            // 处理天然气DCS值
            String propCode = settingEntry.getKey();
            SnapshotTplSettingDetailDTO settingDetailDTO = settingEntry.getValue();
            SnapshotGasEntity snapshotGasEntity = gasEntityMap.get(settingDetailDTO.getDataCode());
            if (snapshotGasEntity != null) {
                // 数字孪生获取数据
                BigDecimal dataVal = secDataCodeValJsonObj.getBigDecimal(propCode);
                snapshotGasEntity.setGasDcsVal(dataVal);
            }
        }
    }
}
