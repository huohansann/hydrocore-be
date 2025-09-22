package com.siact.module.base.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.constant.ConstantTime;
import com.siact.common.utils.ConvertUtils;
import com.siact.module.base.dto.ControlIntervalConfigChartDTO;
import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.entity.ControlIntervalConfigEntity;
import com.siact.module.base.mapper.ControlIntervalConfigMapper;
import com.siact.module.base.service.ControlIntervalConfigService;
import com.siact.module.base.vo.ControlIntervalConfigVO;
import com.siact.sec.utils.IntervalTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
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
    @Override
    public List<ControlIntervalConfigDTO> selectListByCondition(ControlIntervalConfigVO configVO) {
        List<String> measurePointList = new ArrayList<>();
        if (StringUtils.isNoneBlank(configVO.getMeasurePoint())) {
            measurePointList = Arrays.asList(configVO.getMeasurePoint().split(","));
        }

        LambdaQueryWrapper<ControlIntervalConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(StringUtils.isNoneBlank(configVO.getMeasurePoint()),ControlIntervalConfigEntity::getMeasurePoint, measurePointList);
        wrapper.eq(StringUtils.isNoneBlank(configVO.getPointType()),ControlIntervalConfigEntity::getPointType, configVO.getPointType());
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

    @Override
    public void add(ControlIntervalConfigDTO configDTO) {
        ControlIntervalConfigEntity configEntity = ConvertUtils.sourceToTarget(configDTO, ControlIntervalConfigEntity.class);
        baseMapper.insert(configEntity);
    }

    @Override
    public void updateConfig(List<ControlIntervalConfigDTO> configDTOs) {
        List<ControlIntervalConfigEntity> configEntities = ConvertUtils.sourceToTarget(configDTOs, ControlIntervalConfigEntity.class);
        configEntities.forEach(configEntity ->baseMapper.updateById(configEntity));
    }

    @Override
    public ControlIntervalConfigDTO get(ControlIntervalConfigVO configVO) {
        if (configVO.getId() != null) {
            // 优先主键查询
            return ConvertUtils.sourceToTarget(baseMapper.selectById(configVO.getId()), ControlIntervalConfigDTO.class);
        }
        LambdaQueryWrapper<ControlIntervalConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNoneBlank(configVO.getMeasurePoint()),ControlIntervalConfigEntity::getMeasurePoint, configVO.getMeasurePoint());
        wrapper.eq(StringUtils.isNoneBlank(configVO.getPointType()),ControlIntervalConfigEntity::getPointType, configVO.getPointType());
        ControlIntervalConfigEntity configEntity = baseMapper.selectOne(wrapper);
        return ConvertUtils.sourceToTarget(configEntity, ControlIntervalConfigDTO.class);
    }

    /**
     * 查询指定时间段内指定测点类型的控制区间设置(暂时不启用)
     *
     * @param configVO 查询条件
     * @return 各个测点数据
     */
    @Override
    public JSONObject selectListByConditionNew(ControlIntervalConfigVO configVO) {
        String startTimeStr = configVO.getStartTime();
        String endTimeStr = configVO.getEndTime();
        Map<String, List<ControlIntervalConfigEntity>> mcMap = getMcMap(configVO, startTimeStr, endTimeStr);

        String tsUnit = configVO.getTsUnit();
        Integer ts = configVO.getTs();
        String formatVal = configVO.getFormatVal();
        // 这里直接获取每个步长的末尾时间，后面取交集使用
        List<String> xAxis = IntervalTimeUtil.queryIntervalTimeList(startTimeStr, endTimeStr, tsUnit, ts, ConstantTime.DATE_TIME);

        LinkedHashMap<String, String> timeMap = convertToEndTime(xAxis, tsUnit);
        JSONObject result = new JSONObject();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatVal);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(ConstantTime.DATE_TIME);

        for (String mc : mcMap.keySet()) {
            List<ControlIntervalConfigEntity> configEntities = mcMap.get(mc);
            List<ControlIntervalConfigDTO> configDTOList = new ArrayList<>();

            for (String timeStr : timeMap.keySet()) {
                LocalDateTime time = LocalDateTime.parse(timeStr, dateTimeFormatter);
                // 找到该时间点的有效配置项
                Optional<ControlIntervalConfigEntity> optionalConfig = configEntities.stream()
                        .filter(config ->
                                {
                                    // 如果 startTime 为 null 或者 time 在 startTime 之后（包括相等）
                                    boolean startTimeCondition =
                                            config.getStartTime() == null || !time.isBefore(config.getStartTime());
                                    // 如果 endTime 为 null 或者 time 在 endTime 之前（包括相等）
                                    boolean endTimeCondition =
                                            config.getEndTime() == null || !time.isAfter(config.getEndTime());

                                    return startTimeCondition && endTimeCondition;
                                }
                        )
                        .findFirst();
                ControlIntervalConfigDTO dto = new ControlIntervalConfigDTO();
                dto.setTime(formatter.format(LocalDateTime.parse(timeMap.get(timeStr), dateTimeFormatter)));
                if (optionalConfig.isPresent()) {
                    ControlIntervalConfigEntity config = optionalConfig.get();
                    BeanUtils.copyProperties(config, dto);
                }
                configDTOList.add(dto);
            }
            result.put(mc, configDTOList);
        }

        return result;
    }

    @Override
    public void updateAndSaveHis(ControlIntervalConfigDTO configDTO) {
        // 根据id将endTime更新为当前时间
        ControlIntervalConfigEntity configEntity = new ControlIntervalConfigEntity();
        configEntity.setId(configDTO.getId());
        configEntity.setEndTime(LocalDateTime.now());
        baseMapper.updateById(configEntity);

        // 将当前配置插入新的记录
        ControlIntervalConfigEntity insertEntity = ConvertUtils.sourceToTarget(configDTO, ControlIntervalConfigEntity.class);
        insertEntity.setId(null);
        insertEntity.setEndTime(null);
        insertEntity.setStartTime(LocalDateTime.now());
        baseMapper.insert(insertEntity);
    }

    @Override
    public ControlIntervalConfigChartDTO chart(ControlIntervalConfigVO configVO) {
        List<ControlIntervalConfigDTO> dataList = selectListByCondition(configVO);

        dataList = dataList.stream().sorted(Comparator.comparingInt(o -> Integer.parseInt(o.getMeasurePoint().split("MC")[1]))).collect(Collectors.toList());

        ControlIntervalConfigChartDTO chartDTO = new ControlIntervalConfigChartDTO();

        // 整理x轴排序(根据MC进行分隔排序)
        List<String> xAxis = dataList.stream().map(ControlIntervalConfigDTO::getMeasurePoint).collect(Collectors.toList());
        xAxis.sort(Comparator.comparingInt(o->Integer.parseInt(o.split("MC")[1])));
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

    @NotNull
    private Map<String, List<ControlIntervalConfigEntity>> getMcMap(ControlIntervalConfigVO configVO, String startTimeStr, String endTimeStr) {
        LambdaQueryWrapper<ControlIntervalConfigEntity> wrapper = new LambdaQueryWrapper<>();

        // 添加测点和点类型的条件
        wrapper.eq(StringUtils.isNoneBlank(configVO.getMeasurePoint()), ControlIntervalConfigEntity::getMeasurePoint, configVO.getMeasurePoint());
        wrapper.eq(StringUtils.isNoneBlank(configVO.getPointType()), ControlIntervalConfigEntity::getPointType, configVO.getPointType());

        if (StringUtils.isNotBlank(startTimeStr) && StringUtils.isNotBlank(endTimeStr)) {
            // 时间范围查询
            wrapper.and(w -> w
                    // start_time <= #{endTime}
                    .le(ControlIntervalConfigEntity::getStartTime, endTimeStr)
                    .and(subW -> subW
                            // end_time >= #{startTime}
                            .ge(ControlIntervalConfigEntity::getEndTime, startTimeStr)
                            // end_time is null
                            .or().isNull(ControlIntervalConfigEntity::getEndTime)
                    )
            );
        } else {
            // 没有时间范围，查询至今有效的配置项
            wrapper.isNull(ControlIntervalConfigEntity::getEndTime);
        }

        // 查询数据
        List<ControlIntervalConfigEntity> controlIntervalConfigEntities = baseMapper.selectList(wrapper);
        return controlIntervalConfigEntities.stream().collect(Collectors.groupingBy(ControlIntervalConfigEntity::getMeasurePoint));
    }

    private LinkedHashMap<String, String> convertToEndTime(List<String> xAxis, String tsUnit) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ConstantTime.DATE_TIME);
        LinkedHashMap<String, String> timeMap = new LinkedHashMap<>();
        for (String dateStr : xAxis) {
            LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
            LocalDateTime endTime;

            switch (tsUnit) {
                case "Y":
                    endTime =dateTime.withMonth(12).withDayOfMonth(31).withHour(23).withMinute(59).withSecond(59);
                    break;
                case "M":
                    endTime =dateTime.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
                    break;
                case "D":
                    endTime =dateTime.withHour(23).withMinute(59).withSecond(59);
                    break;
                case "H":
                    endTime =dateTime.withMinute(59).withSecond(59);
                    break;
                default:
                    endTime = dateTime;
            }

            timeMap.put(endTime.format(formatter),  dateStr);
        }

        return timeMap;
    }
}
