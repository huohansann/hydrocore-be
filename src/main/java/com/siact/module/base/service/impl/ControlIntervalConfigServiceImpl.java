package com.siact.module.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.siact.sec.utils.IntervalTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 控制区间设置
 *
 * @author wr
 */
@Service
@Slf4j
public class ControlIntervalConfigServiceImpl extends ServiceImpl<ControlIntervalConfigMapper, ControlIntervalConfigEntity> implements ControlIntervalConfigService {
    @Override
    public List<ControlIntervalConfigDTO> selectListByCondition(ControlIntervalConfigVO configVO) {
        List<String> measurePointList = new ArrayList<>();
        if (StringUtils.isNoneBlank(configVO.getMeasurePoint())) {
            measurePointList = Arrays.asList(configVO.getMeasurePoint().split(","));
        }

        LambdaQueryWrapper<ControlIntervalConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(StringUtils.isNoneBlank(configVO.getMeasurePoint()), ControlIntervalConfigEntity::getMeasurePoint, measurePointList);
        wrapper.eq(StringUtils.isNoneBlank(configVO.getPointType()), ControlIntervalConfigEntity::getPointType, configVO.getPointType());
        // 逻辑删除,查询正常状态数据
        wrapper.eq(ControlIntervalConfigEntity::getDeleteFlag, false);
        // 没有条件就默认查询全部
        List<ControlIntervalConfigEntity> controlIntervalConfigEntities = baseMapper.selectList(wrapper);
        return ConvertUtils.sourceToTarget(controlIntervalConfigEntities, ControlIntervalConfigDTO.class);
    }

    @Override
    public List<ControlIntervalConfigDTO> selectListByDataCodeList(List<String> dataCodeList) {
        if (ObjectUtils.isEmpty(dataCodeList)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<ControlIntervalConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ControlIntervalConfigEntity::getDataCode, dataCodeList);

        List<ControlIntervalConfigEntity> controlIntervalConfigEntities = baseMapper.selectList(wrapper);
        return ConvertUtils.sourceToTarget(controlIntervalConfigEntities, ControlIntervalConfigDTO.class);
    }

//    @Override
//    public void add(ControlIntervalConfigDTO configDTO) {
//        ControlIntervalConfigEntity configEntity = ConvertUtils.sourceToTarget(configDTO, ControlIntervalConfigEntity.class);
//        // 新增,设置删除状态为0
//        configEntity.setDeleteFlag(false);
//        baseMapper.insert(configEntity);
//    }

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

    /**
     * 查询指定时间段内指定测点类型的控制区间设置(暂时不启用)
     *
     * @param configVO 查询条件
     * @return 各个测点数据
     */
//    @Override
//    public JSONObject selectListByConditionNew(ControlIntervalConfigVO configVO) {
//        String startTimeStr = configVO.getStartTime();
//        String endTimeStr = configVO.getEndTime();
//        Map<String, List<ControlIntervalConfigEntity>> mcMap = getMcMap(configVO, startTimeStr, endTimeStr);
//
//        String tsUnit = configVO.getTsUnit();
//        Integer ts = configVO.getTs();
//        String formatVal = configVO.getFormatVal();
//        // 这里直接获取每个步长的末尾时间，后面取交集使用
//        List<String> xAxis = IntervalTimeUtil.queryIntervalTimeList(startTimeStr, endTimeStr, tsUnit, ts, ConstantTime.DATE_TIME);
//
//        LinkedHashMap<String, String> timeMap = convertToEndTime(xAxis, tsUnit);
//        JSONObject result = new JSONObject();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatVal);
//        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(ConstantTime.DATE_TIME);
//
//        for (String mc : mcMap.keySet()) {
//            List<ControlIntervalConfigEntity> configEntities = mcMap.get(mc);
//            List<ControlIntervalConfigDTO> configDTOList = new ArrayList<>();
//
//            for (String timeStr : timeMap.keySet()) {
//                LocalDateTime time = LocalDateTime.parse(timeStr, dateTimeFormatter);
//                // 找到该时间点的有效配置项
//                Optional<ControlIntervalConfigEntity> optionalConfig = configEntities.stream()
//                        .filter(config ->
//                                {
//                                    // 如果 startTime 为 null 或者 time 在 startTime 之后（包括相等）
//                                    boolean startTimeCondition =
//                                            config.getStartTime() == null || !time.isBefore(config.getStartTime());
//                                    // 如果 endTime 为 null 或者 time 在 endTime 之前（包括相等）
//                                    boolean endTimeCondition =
//                                            config.getEndTime() == null || !time.isAfter(config.getEndTime());
//
//                                    return startTimeCondition && endTimeCondition;
//                                }
//                        )
//                        .findFirst();
//                ControlIntervalConfigDTO dto = new ControlIntervalConfigDTO();
//                dto.setTime(formatter.format(LocalDateTime.parse(timeMap.get(timeStr), dateTimeFormatter)));
//                if (optionalConfig.isPresent()) {
//                    ControlIntervalConfigEntity config = optionalConfig.get();
//                    BeanUtils.copyProperties(config, dto);
//                }
//                configDTOList.add(dto);
//            }
//            result.put(mc, configDTOList);
//        }
//
//        return result;
//    }

//    @Override
//    public void updateAndSaveHis(ControlIntervalConfigDTO configDTO) {
//        // 根据id将endTime更新为当前时间
//        ControlIntervalConfigEntity configEntity = new ControlIntervalConfigEntity();
//        configEntity.setId(configDTO.getId());
//        configEntity.setEndTime(LocalDateTime.now());
//        baseMapper.updateById(configEntity);
//
//        // 将当前配置插入新的记录
//        ControlIntervalConfigEntity insertEntity = ConvertUtils.sourceToTarget(configDTO, ControlIntervalConfigEntity.class);
//        insertEntity.setId(null);
//        insertEntity.setEndTime(null);
//        insertEntity.setStartTime(LocalDateTime.now());
//        baseMapper.insert(insertEntity);
//    }
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

        // 1:查询时间范围的所有的数据
        // 2:根据时间范围查询数据 包含 历史事件范围的配置信息 及  当前生效的配置信息(未删除)
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


        List<ControlIntervalConfigEntity> configEntities = baseMapper.selectList(wrapper);

        // k:dataCode v:当前dataCode下的所有配置
        Map<String, List<ControlIntervalConfigEntity>> dataCodeConfigMap = configEntities.stream().collect(Collectors.groupingBy(ControlIntervalConfigEntity::getDataCode));

        // 3:获取时间轴
        List<String> timeList = IntervalTimeUtil.queryIntervalTimeList(startTime, endTime, tsUnit, ts, ConstantTime.DATE_TIME);

        // 初始化返回值
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

        // 遍历dataCodeConfigMap 补充config数据值
        for (Map.Entry<String, List<ControlIntervalConfigEntity>> config : dataCodeConfigMap.entrySet()) {

            // 当前元素的dataCode
            String curDataCode = config.getKey();

            ControlIntervalConfigHisChartDataDTO dataDTO = result.get(curDataCode);
            List<Object[]> upControlChart = dataDTO.getUpControlChart();
            List<Object[]> downControlChart = dataDTO.getLowControlChart();
            List<Object[]> upAlarmChart = dataDTO.getUpAlarmChart();
            List<Object[]> downAlarmChart = dataDTO.getLowAlarmChart();
            List<Object[]> temperatureSetChart = dataDTO.getTemperatureSetChart();

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
                    upControlChart.set(index, new Object[]{timeFormat, configEntity.getUpControl()});
                    downControlChart.set(index, new Object[]{timeFormat, configEntity.getLowControl()});
                    upAlarmChart.set(index, new Object[]{timeFormat, configEntity.getUpAlarm()});
                    downAlarmChart.set(index, new Object[]{timeFormat, configEntity.getLowAlarm()});
                    temperatureSetChart.set(index, new Object[]{timeFormat, configEntity.getTemperatureSet()});
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

        return result;
    }

//    @NotNull
//    private Map<String, List<ControlIntervalConfigEntity>> getMcMap(ControlIntervalConfigVO configVO, String startTimeStr, String endTimeStr) {
//        LambdaQueryWrapper<ControlIntervalConfigEntity> wrapper = new LambdaQueryWrapper<>();
//
//        // 添加测点和点类型的条件
//        wrapper.eq(StringUtils.isNoneBlank(configVO.getMeasurePoint()), ControlIntervalConfigEntity::getMeasurePoint, configVO.getMeasurePoint());
//        wrapper.eq(StringUtils.isNoneBlank(configVO.getPointType()), ControlIntervalConfigEntity::getPointType, configVO.getPointType());
//
//        if (StringUtils.isNotBlank(startTimeStr) && StringUtils.isNotBlank(endTimeStr)) {
//            // 时间范围查询
//            wrapper.and(w -> w
//                    // start_time <= #{endTime}
//                    .le(ControlIntervalConfigEntity::getStartTime, endTimeStr)
//                    .and(subW -> subW
//                            // end_time >= #{startTime}
//                            .ge(ControlIntervalConfigEntity::getEndTime, startTimeStr)
//                            // end_time is null
//                            .or().isNull(ControlIntervalConfigEntity::getEndTime)
//                    )
//            );
//        } else {
//            // 没有时间范围，查询至今有效的配置项
//            wrapper.isNull(ControlIntervalConfigEntity::getEndTime);
//        }
//
//        // 查询数据
//        List<ControlIntervalConfigEntity> controlIntervalConfigEntities = baseMapper.selectList(wrapper);
//        return controlIntervalConfigEntities.stream().collect(Collectors.groupingBy(ControlIntervalConfigEntity::getMeasurePoint));
//    }

    private LinkedHashMap<String, String> convertToEndTime(List<String> xAxis, String tsUnit) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ConstantTime.DATE_TIME);
        LinkedHashMap<String, String> timeMap = new LinkedHashMap<>();
        for (String dateStr : xAxis) {
            LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
            LocalDateTime endTime;

            switch (tsUnit) {
                case "Y":
                    endTime = dateTime.withMonth(12).withDayOfMonth(31).withHour(23).withMinute(59).withSecond(59);
                    break;
                case "M":
                    endTime = dateTime.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
                    break;
                case "D":
                    endTime = dateTime.withHour(23).withMinute(59).withSecond(59);
                    break;
                case "H":
                    endTime = dateTime.withMinute(59).withSecond(59);
                    break;
                default:
                    endTime = dateTime;
            }

            timeMap.put(endTime.format(formatter), dateStr);
        }

        return timeMap;
    }
}
