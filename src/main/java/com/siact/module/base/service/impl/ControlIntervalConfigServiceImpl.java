package com.siact.module.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.config.KilnProperty;
import com.siact.common.constant.ConstantDataApi;
import com.siact.common.constant.ConstantNum;
import com.siact.common.constant.ConstantTime;
import com.siact.common.utils.ConvertUtils;
import com.siact.common.utils.TimeUtil;
import com.siact.module.base.dto.ControlIntervalConfigChartDTO;
import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.dto.ControlIntervalConfigHisChartDataDTO;
import com.siact.module.base.entity.ControlIntervalConfigEntity;
import com.siact.module.base.mapper.ControlIntervalConfigMapper;
import com.siact.module.base.service.ControlIntervalConfigService;
import com.siact.module.base.vo.ControlIntervalConfigVO;
import com.siact.sec.dto.IntervalDataDto;
import com.siact.sec.dto.IntervalValParamsDto;
import com.siact.sec.sevice.impl.DataServiceImpl;
import com.siact.sec.utils.IntervalTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 控制区间设置
 *
 * @author wr
 */
@Service
@Slf4j
public class ControlIntervalConfigServiceImpl extends ServiceImpl<ControlIntervalConfigMapper, ControlIntervalConfigEntity> implements ControlIntervalConfigService {
    private @Resource DataServiceImpl dataService;
    private @Resource KilnProperty property;

    @Override
    public List<ControlIntervalConfigDTO> selectListByCondition(ControlIntervalConfigVO configVO) {
        List<String> measurePointList = new ArrayList<>();
        if (StringUtils.isNoneBlank(configVO.getMeasurePoint())) {
            measurePointList = Arrays.asList(configVO.getMeasurePoint().split(","));
        }

        LambdaQueryWrapper<ControlIntervalConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(StringUtils.isNoneBlank(configVO.getMeasurePoint()), ControlIntervalConfigEntity::getMeasurePoint, measurePointList);
        wrapper.eq(StringUtils.isNoneBlank(configVO.getPointType()), ControlIntervalConfigEntity::getPointType, configVO.getPointType());
        // 查询正常状态数据
        wrapper.eq(ControlIntervalConfigEntity::getDeleteFlag, false);
        // 没有条件就默认查询全部
        List<ControlIntervalConfigEntity> controlIntervalConfigEntities = baseMapper.selectList(wrapper);
        // 根据 measurePoint 进行排序
        controlIntervalConfigEntities.sort(Comparator.comparingInt(item -> Integer.parseInt(item.getMeasurePoint().replace("MC", ""))));
        return ConvertUtils.sourceToTarget(controlIntervalConfigEntities, ControlIntervalConfigDTO.class);
    }

    @Override
    public List<ControlIntervalConfigDTO> selectListByDataCodeList(List<String> dataCodeList) {
        if (ObjectUtils.isEmpty(dataCodeList)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<ControlIntervalConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ControlIntervalConfigEntity::getDataCode, dataCodeList);
        // 查询正常状态数据
        wrapper.eq(ControlIntervalConfigEntity::getDeleteFlag, false);

        List<ControlIntervalConfigEntity> controlIntervalConfigEntities = baseMapper.selectList(wrapper);
        return ConvertUtils.sourceToTarget(controlIntervalConfigEntities, ControlIntervalConfigDTO.class);
    }


    @Override
    @Transactional
    public void updateConfig(List<ControlIntervalConfigDTO> configDTOs) {

        String nowStr = TimeUtil.getNowStr(ConstantTime.DATE_TIME_MM_00);

        // 逻辑删除,设置删除状态为1(ps:即保留历史数据)
        LambdaUpdateWrapper<ControlIntervalConfigEntity> updateWrapper = new LambdaUpdateWrapper<>();
        List<String> dataCodeList = configDTOs.stream().map(ControlIntervalConfigDTO::getDataCode).collect(Collectors.toList());
        updateWrapper.set(ControlIntervalConfigEntity::getDeleteFlag, true);
        updateWrapper.set(ControlIntervalConfigEntity::getEndTime, nowStr);
        updateWrapper.in(ControlIntervalConfigEntity::getDataCode, dataCodeList);
        updateWrapper.eq(ControlIntervalConfigEntity::getDeleteFlag, false);
        baseMapper.update(null, updateWrapper);

        // 再新增
        List<ControlIntervalConfigEntity> configEntities = ConvertUtils.sourceToTarget(configDTOs, ControlIntervalConfigEntity.class);
        for (ControlIntervalConfigEntity insertEntity : configEntities) {
            insertEntity.setId(null);
            // 时间精确到分钟
            insertEntity.setStartTime(nowStr);
            insertEntity.setEndTime(null);
            insertEntity.setDeleteFlag(false);
        }
        saveBatch(configEntities);
    }

    @Override
    public @Transactional() void sync() {
        // 查询当前记录数据
        LambdaQueryWrapper<ControlIntervalConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ControlIntervalConfigEntity::getDeleteFlag, false);
        wrapper.eq(ControlIntervalConfigEntity::getPointType, "temperature");
        List<ControlIntervalConfigEntity> entities = baseMapper.selectList(wrapper);
        // 获取所有编码
        List<String> dataCodes = entities.stream().map(ControlIntervalConfigEntity::getDataCode).collect(Collectors.toList());

        IntervalValParamsDto paramDTO = new IntervalValParamsDto();
        // 当前时间
        LocalDateTime now = LocalDateTime.now().withSecond(0);
        paramDTO.setDataCodes(dataCodes);
        // 开始时间设置到最大换火周期区间前的时刻
        Integer maxRange = property.getIntervalControl().values().stream().map(KilnProperty.IntervalControl::getRange).flatMap(List::stream).max( Integer::compare).orElse(4);
        paramDTO.setStartTime(now.minusMinutes(property.getConfig().getFireChangeCycle() * maxRange).format(TimeUtil.df));
        paramDTO.setEndTime(now.format(TimeUtil.df));
        paramDTO.setTs(ConstantNum.NUMBER_ONE);
        paramDTO.setTsUnit(ConstantDataApi.TS_MIN);
        paramDTO.setCalcType(ConstantDataApi.CALC_LAST);
        paramDTO.setFormatVal(ConstantTime.DATE_TIME_MM_00);
        List<IntervalDataDto> intervalList = dataService.queryIntervalVal(paramDTO);
        // 根据 dataCode 进行分组
        Map<String, List<IntervalDataDto>> intervalMap = intervalList.stream().collect(Collectors.groupingBy(IntervalDataDto::getDataCode));

        // 更新非关键点位
        this.updateConfig(buildConfigSyncParams(intervalMap, entities, now));
    }

    @Override
    public @Transactional void saveAndSyncConfig(List<ControlIntervalConfigDTO> configDTOs) {
        // 先保存
        this.updateConfig(configDTOs);
        // 再同步其他非关键点
        this.sync();
    }

    @Override
    public ControlIntervalConfigDTO get(ControlIntervalConfigVO configVO) {
        if (configVO.getId() != null) {
            // 优先主键查询
            return ConvertUtils.sourceToTarget(baseMapper.selectById(configVO.getId()), ControlIntervalConfigDTO.class);
        }
        LambdaQueryWrapper<ControlIntervalConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNoneBlank(configVO.getMeasurePoint()), ControlIntervalConfigEntity::getMeasurePoint, configVO.getMeasurePoint());
        wrapper.eq(StringUtils.isNoneBlank(configVO.getPointType()), ControlIntervalConfigEntity::getPointType, configVO.getPointType());
        // 逻辑删除,查询正常状态数据
        wrapper.eq(ControlIntervalConfigEntity::getDeleteFlag, false);
        ControlIntervalConfigEntity configEntity = baseMapper.selectOne(wrapper);
        return ConvertUtils.sourceToTarget(configEntity, ControlIntervalConfigDTO.class);
    }

    @Override
    public ControlIntervalConfigChartDTO chart(ControlIntervalConfigVO configVO) {
        List<ControlIntervalConfigDTO> dataList = selectListByCondition(configVO);

        dataList = dataList.stream().sorted(Comparator.comparingInt(o -> Integer.parseInt(o.getMeasurePoint().split("MC")[1]))).collect(Collectors.toList());

        ControlIntervalConfigChartDTO chartDTO = new ControlIntervalConfigChartDTO();

        // 整理x轴排序(根据MC进行分隔排序)
        List<String> xAxis = dataList.stream().map(ControlIntervalConfigDTO::getMeasurePoint).collect(Collectors.toList());
        xAxis.sort(Comparator.comparingInt(o -> Integer.parseInt(o.split("MC")[1])));
        chartDTO.setXAxis(xAxis);

        // 整理数据
        List<Object[]> upControlData = new ArrayList<>();
        List<Object[]> lowControlData = new ArrayList<>();
        List<Object[]> upAlarmData = new ArrayList<>();
        List<Object[]> lowAlarmData = new ArrayList<>();
        List<Object[]> temperatureSetData = new ArrayList<>();
        for (ControlIntervalConfigDTO value : dataList) {

            upControlData.add(new Object[]{value.getMeasurePoint(), value.getUpControl()});
            lowControlData.add(new Object[]{value.getMeasurePoint(), value.getLowControl()});
            upAlarmData.add(new Object[]{value.getMeasurePoint(), value.getUpAlarm()});
            lowAlarmData.add(new Object[]{value.getMeasurePoint(), value.getLowAlarm()});
            temperatureSetData.add(new Object[]{value.getMeasurePoint(), value.getTemperatureSet()});
        }

        // 设置图表数据
        chartDTO.setUpControlData(upControlData);
        chartDTO.setLowControlData(lowControlData);
        chartDTO.setUpAlarmData(upAlarmData);
        chartDTO.setLowAlarmData(lowAlarmData);
        chartDTO.setTemperatureSetData(temperatureSetData);
        return chartDTO;
    }


    @Override
    public Map<String, ControlIntervalConfigHisChartDataDTO> queryHistoryConfigChart(List<String> dataCodeList,
                                                                                     String startTime, String endTime,
                                                                                     Integer ts, String tsUnit, String formatVal) {

        // 1:根据时间范围查询数据 包含 历史事件范围的配置信息 及  当前生效的配置信息(未删除)
        List<ControlIntervalConfigEntity> configEntities = queryHistoryConfigInRange(dataCodeList, startTime, endTime);

        // k:dataCode v:当前dataCode下的所有配置
        Map<String, List<ControlIntervalConfigEntity>> dataCodeConfigMap = configEntities.stream().collect(Collectors.groupingBy(ControlIntervalConfigEntity::getDataCode));

        // 2:获取时间轴 (这里要是全时间,需要跟entity的全时间进行匹配)
        List<String> timeList = IntervalTimeUtil.queryIntervalTimeList(startTime, endTime, tsUnit, ts, ConstantTime.DATE_TIME);

        // 3:初始化返回值
        Map<String, ControlIntervalConfigHisChartDataDTO> result = initHistoryConfigChartRtnData(dataCodeList, timeList);

        // 4:遍历dataCodeConfigMap 补充config数据值
        calcHistoryConfigRtnData(formatVal, dataCodeConfigMap, result, timeList);

        return result;
    }

    private List<ControlIntervalConfigEntity> queryHistoryConfigInRange(List<String> dataCodeList, String startTime, String endTime) {
        LambdaQueryWrapper<ControlIntervalConfigEntity> wrapper = new LambdaQueryWrapper<>();

        // 当前生效中
        wrapper.and(w -> w
                .in(ControlIntervalConfigEntity::getDataCode, dataCodeList)
                .eq(ControlIntervalConfigEntity::getDeleteFlag, false));
        // 已失效 区间范围内
        wrapper.or(w -> w
                .in(ControlIntervalConfigEntity::getDataCode, dataCodeList)
                .eq(ControlIntervalConfigEntity::getDeleteFlag, true)
                .ge(ControlIntervalConfigEntity::getStartTime, startTime)
                .le(ControlIntervalConfigEntity::getEndTime, endTime));
        // 已失效 跨区间  1:startTime跨区间 (左包右不包)
        wrapper.or(w -> w
                .in(ControlIntervalConfigEntity::getDataCode, dataCodeList)
                .eq(ControlIntervalConfigEntity::getDeleteFlag, true)
                .le(ControlIntervalConfigEntity::getStartTime, startTime)
                .gt(ControlIntervalConfigEntity::getEndTime, startTime));
        // 已失效 跨区间  2:endTime跨区间 (左不包右包)
        wrapper.or(w -> w
                .in(ControlIntervalConfigEntity::getDataCode, dataCodeList)
                .eq(ControlIntervalConfigEntity::getDeleteFlag, true)
                .le(ControlIntervalConfigEntity::getStartTime, endTime)
                .gt(ControlIntervalConfigEntity::getEndTime, endTime));

        return baseMapper.selectList(wrapper);
    }

    @NotNull
    private static Map<String, ControlIntervalConfigHisChartDataDTO> initHistoryConfigChartRtnData(List<String> dataCodeList, List<String> timeList) {
        Map<String, ControlIntervalConfigHisChartDataDTO> result = new HashMap<>();

        for (String dataCode : dataCodeList) {
            List<Object[]> upControlChart = new ArrayList<>();
            List<Object[]> lowControlChart = new ArrayList<>();
            List<Object[]> upAlarmChart = new ArrayList<>();
            List<Object[]> lowAlarmChart = new ArrayList<>();
            List<Object[]> temperatureSetChart = new ArrayList<>();

            for (String time : timeList) {
                upControlChart.add(new Object[]{time, null});
                lowControlChart.add(new Object[]{time, null});
                upAlarmChart.add(new Object[]{time, null});
                lowAlarmChart.add(new Object[]{time, null});
                temperatureSetChart.add(new Object[]{time, null});
            }

            result.put(dataCode, ControlIntervalConfigHisChartDataDTO.builder()
                    .upControlChart(upControlChart)
                    .lowControlChart(lowControlChart)
                    .upAlarmChart(upAlarmChart)
                    .lowAlarmChart(lowAlarmChart)
                    .temperatureSetChart(temperatureSetChart)
                    .build());
        }
        return result;
    }

    private static void calcHistoryConfigRtnData(String formatVal, Map<String, List<ControlIntervalConfigEntity>> dataCodeConfigMap, Map<String, ControlIntervalConfigHisChartDataDTO> result, List<String> timeList) {
        // 遍历每个dataCode下的配置
        for (Map.Entry<String, List<ControlIntervalConfigEntity>> config : dataCodeConfigMap.entrySet()) {

            // 当前元素的dataCode
            String curDataCode = config.getKey();
            // 当前dataCode的返回值
            ControlIntervalConfigHisChartDataDTO dataDTO = result.get(curDataCode);
            String maxUpControlVal = null;
            String minLowControlVal = null;
            String maxUpAlarmVal = null;
            String minLowAlarmVal = null;
            String maxTemperatureSetVal = null;
            String minTemperatureSetVal = null;

            // 找到该时间点的有效配置项
            for (ControlIntervalConfigEntity configEntity : config.getValue()) {
                ListIterator<String> timeIterator = timeList.listIterator();
                while (timeIterator.hasNext()) {
                    String time = timeIterator.next();
                    int index = timeIterator.previousIndex();
                    String effectiveTime = IntervalTimeUtil.dateFormat(configEntity.getStartTime(), ConstantTime.DATE_TIME);// 这里要全时间匹配
                    if (time.compareTo(effectiveTime) < 0) {
                        // 时间点早于配置生效时间，不做填充
                        continue;
                    }
                    // 找到该时间点的有效配置项
                    String timeFormat = IntervalTimeUtil.dateFormat(time, formatVal);
                    dataDTO.getUpControlChart().set(index, new Object[]{timeFormat, configEntity.getUpControl()});
                    dataDTO.getLowControlChart().set(index, new Object[]{timeFormat, configEntity.getLowControl()});
                    dataDTO.getUpAlarmChart().set(index, new Object[]{timeFormat, configEntity.getUpAlarm()});
                    dataDTO.getLowAlarmChart().set(index, new Object[]{timeFormat, configEntity.getLowAlarm()});
                    dataDTO.getTemperatureSetChart().set(index, new Object[]{timeFormat, configEntity.getTemperatureSet()});
                }

                // 更新最大最小值
                maxUpControlVal = maxUpControlVal == null ? configEntity.getUpControl() : maxUpControlVal.compareTo(configEntity.getUpControl()) > 0 ? maxUpControlVal : configEntity.getUpControl();
                minLowControlVal = minLowControlVal == null ? configEntity.getLowControl() : minLowControlVal.compareTo(configEntity.getLowControl()) < 0 ? minLowControlVal : configEntity.getLowControl();
                maxUpAlarmVal = maxUpAlarmVal == null ? configEntity.getUpAlarm() : maxUpAlarmVal.compareTo(configEntity.getUpAlarm()) > 0 ? maxUpAlarmVal : configEntity.getUpAlarm();
                minLowAlarmVal = minLowAlarmVal == null ? configEntity.getLowAlarm() : minLowAlarmVal.compareTo(configEntity.getLowAlarm()) < 0 ? minLowAlarmVal : configEntity.getLowAlarm();
                maxTemperatureSetVal = maxTemperatureSetVal == null ? configEntity.getTemperatureSet() : maxTemperatureSetVal.compareTo(configEntity.getTemperatureSet()) > 0 ? maxTemperatureSetVal : configEntity.getTemperatureSet();
                minTemperatureSetVal = minTemperatureSetVal == null ? configEntity.getTemperatureSet() : minTemperatureSetVal.compareTo(configEntity.getTemperatureSet()) < 0 ? minTemperatureSetVal : configEntity.getTemperatureSet();
            }

            // 补充dataDTO的最大最小值
            dataDTO.setMaxUpControlVal(maxUpControlVal);
            dataDTO.setMinLowControlVal(minLowControlVal);
            dataDTO.setMaxUpAlarmVal(maxUpAlarmVal);
            dataDTO.setMinLowAlarmVal(minLowAlarmVal);
            dataDTO.setMaxTemperatureSetVal(maxTemperatureSetVal);
            dataDTO.setMinTemperatureSetVal(minTemperatureSetVal);

            result.put(curDataCode, dataDTO);
        }
    }

    private List<ControlIntervalConfigDTO> buildConfigSyncParams(Map<String, List<IntervalDataDto>> intervalMap, List<ControlIntervalConfigEntity> entities, LocalDateTime now) {
        // 关键点位
        List<String> keyPoints = Arrays.asList("MC1", "MC4", "MC5", "MC10");
        // 换火周期
        long fireChangeCycle = property.getConfig().getFireChangeCycle();
        // 自动计算配置
        Map<String, KilnProperty.IntervalControl> icmap = property.getIntervalControl();
        // 获取计算参数
        HashMap<String, Map<String, BigDecimal>> results = new HashMap<>();
        intervalMap.forEach((dataCode, intervals) -> {
            // 获取对应配置
            ControlIntervalConfigEntity configEntity = entities.stream().filter(entity -> entity.getDataCode().equals(dataCode)).collect(Collectors.toList()).get(0);
            // 获取当前点位配置项
            KilnProperty.IntervalControl intervalControl = icmap.get(configEntity.getMeasurePoint());
            // 获取平均温度换火周期配置
            List<Integer> range = intervalControl.getRange();
            HashMap<String, BigDecimal> point = new HashMap<>();
            // 计算两个换火周期平均温度
            List<BigDecimal> values = intervals.stream().filter(Objects::nonNull).filter(dto -> {
                        LocalDateTime time = LocalDateTime.parse(dto.getTime(), DateTimeFormatter.ofPattern(ConstantTime.DATE_TIME_MM_00));
                        LocalDateTime end = now.minusMinutes(fireChangeCycle * range.get(1)).withNano(0);
                        LocalDateTime start = now.minusMinutes(fireChangeCycle * range.get(0)).withNano(0);
                        // dto.setItemVal(BigDecimal.valueOf(1450));
                        return (time.isAfter(start) || time.isEqual(start)) && (time.isBefore(end) || time.isEqual(end));
                    })
                    .map(IntervalDataDto::getItemVal)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            // 平均温度
            BigDecimal Tave = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
            // 获取上下限差值一半
            // BigDecimal SPD = ((new BigDecimal(configEntity.getUpControl())).subtract(new BigDecimal(configEntity.getLowControl()))).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            BigDecimal SPD = intervalControl.getSpd().divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            point.put("Tave", Tave);
            point.put("SPD", SPD);
            if (keyPoints.contains(configEntity.getMeasurePoint())) point.put("SP", new BigDecimal(configEntity.getTemperatureSet()));
            results.put(configEntity.getMeasurePoint(), point);
            log.info("控制区间设置自动同步, 点位名称: {}, 温度数据: {}, 平均温度: {}, 上下限差值一半: {}", configEntity.getMeasurePoint(), values, Tave, SPD);
        });

        // 获取关键点位数据
        Map<String, BigDecimal> mc1 = results.get("MC1");
        Map<String, BigDecimal> mc4 = results.get("MC4");
        Map<String, BigDecimal> mc5 = results.get("MC5");
        Map<String, BigDecimal> mc10 = results.get("MC10");

        // 计算目标值和上下限
        for (ControlIntervalConfigEntity entity : entities) {
            BigDecimal SP = new BigDecimal(entity.getTemperatureSet());
            Map<String, BigDecimal> data = results.get(entity.getMeasurePoint());
            BigDecimal SPD = data.get("SPD");
            // 获取告警限差值
            List<Integer> diffValue = icmap.get(entity.getMeasurePoint()).getDiffValue();
            int low = diffValue.get(0);
            int up = diffValue.size() > 1 ? diffValue.get(1) : diffValue.get(0);

            BigDecimal coefficient = BigDecimal.valueOf(0.5);
            // 计算目标值
            switch (entity.getMeasurePoint()) {
                case "MC2":
                case "MC6":
                    Map<String, BigDecimal> mc1_5 = "MC2".equals(entity.getMeasurePoint()) ? mc1 : mc5;
                    Map<String, BigDecimal> mc4_10 = "MC2".equals(entity.getMeasurePoint()) ? mc4 : mc10;
                    SP = data.get("Tave").add(coefficient.multiply(mc1_5.get("SP").subtract(mc1_5.get("Tave"))).multiply(SPD).divide(mc1_5.get("SPD"), 2, RoundingMode.HALF_UP))
                            .add(coefficient.multiply(mc4_10.get("SP").subtract(mc4_10.get("Tave"))).multiply(SPD).divide(mc4_10.get("SPD"), 2, RoundingMode.HALF_UP));
                    break;
                case "MC3":
                    SP = data.get("Tave").add((mc4.get("SP").subtract(mc4.get("Tave"))).multiply(SPD).divide(mc4.get("SPD"), 2, RoundingMode.HALF_UP));
                    break;
                case "MC7":
                case "MC8":
                case "MC9":
                    SP = data.get("Tave").add((mc10.get("SP").subtract(mc10.get("Tave"))).multiply(SPD).divide(mc10.get("SPD"), 2, RoundingMode.HALF_UP));
                    break;
                default:
                    // 关键点位不修改
                    break;
            }
            // 设置非关键点位的目标值
            if (!keyPoints.contains(entity.getMeasurePoint())) entity.setTemperatureSet(SP.setScale(1, RoundingMode.HALF_UP).toString());
            // 计算上控制限
            BigDecimal SPH = SPD.add(SP).setScale(1, RoundingMode.HALF_UP);
            // 设置上控制限
            entity.setUpControl(SPH.toString());
            // 计算下控制限
            BigDecimal SPL = SP.subtract(SPD).setScale(1, RoundingMode.HALF_UP);
            // 设置下控制限
            entity.setLowControl(SPL.toString());
            // 设置上告警限
            entity.setUpAlarm(SPH.add(BigDecimal.valueOf(up)).toString());
            // 设置下告警限
            entity.setLowAlarm(SPL.subtract(BigDecimal.valueOf(low)).toString());
            log.info("控制区间设置自动同步, 点位数据: {}", entity);
        }

        return ConvertUtils.sourceToTarget(entities, ControlIntervalConfigDTO.class);
    }
}
