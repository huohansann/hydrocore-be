package com.siact.hydrocore.common.utils;

import com.siact.hydrocore.sec.dto.IntervalValParamsDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;


/**
 * @desc: 公共方法实现类
 * @author: zhangwentao
 * @create: 2025-04-15 16:46
 */
@Slf4j
@Service
public class ParamConversionUtil {

    /**
     * @param parmsObj
     * @return
     * @desc: 解析柱状图参数
     */
    public static IntervalValParamsDto parseAttributeParams(Object parmsObj) {
        IntervalValParamsDto paramsDto = ConvertUtils.sourceToTarget(parmsObj, IntervalValParamsDto.class);
        parseParms(paramsDto);
        return paramsDto;
    }

    /**
     * 给为空的参数设置默认值
     *
     * @param paramsDto
     */
    public static void parseParms(IntervalValParamsDto paramsDto) {
        // 时间都不为空
        if (StringUtils.isNotBlank(paramsDto.getStartTime()) && StringUtils.isNotBlank(paramsDto.getEndTime())) {
            String time = paramsDto.getStartTime();
            // 判断时间格式
            String startTime = TimeFormatConverter.convertToFullFormat(paramsDto.getStartTime(), 0);
            String endTime = TimeFormatConverter.convertToFullFormat(paramsDto.getEndTime(), 1);
            paramsDto.setStartTime(startTime);
            paramsDto.setEndTime(endTime);
            setValueByTime(paramsDto, time);
        }
        // 开始时间不为空
        else if (StringUtils.isNotBlank(paramsDto.getStartTime())) {
            String startTime = paramsDto.getStartTime();
            // 按照时间，推断时间单位，返回时间格式等
            setValueByTime(paramsDto, startTime);
            // 按照开始时间，获取结束时间
            String timeInterval = TimeFormatConverter.generateRangeTime(startTime);
            String[] split = timeInterval.split("至");
            // 如果开始时间的格式不符合要求，则重新赋值
            if (startTime.length() != 19) {
                paramsDto.setStartTime(split[0]);
            }
            paramsDto.setEndTime(split[1]);
        }
        // 结束时间不为空
        else if (StringUtils.isNotBlank(paramsDto.getEndTime())) {
            String endTime = paramsDto.getEndTime();
            // 按照结束时间，推断时间单位，返回时间格式等
            setValueByTime(paramsDto, endTime);
            // 按照开始时间，获取结束时间
            String timeInterval = TimeFormatConverter.generateRangeTime(endTime);
            String[] split = timeInterval.split("至");
            // 设置默认值:如果开始时间和结束时间，单位都为空的话，则默认按照天维度,西安市月初到现在的数据，以天维度获取
            paramsDto.setStartTime(split[0]);
            if (endTime.length() != 19) {
                paramsDto.setEndTime(split[1]);
            }
        }
        // 时间都为空
        else {
            // 如果时间间距不为空，则按照时间间距来获取时间
            if (StringUtils.isNotBlank(paramsDto.getTsUnit())) {
                String timeInterval = TimeFormatConverter.generateRangeByUnit(paramsDto.getTsUnit());
                String[] split = timeInterval.split("至");
                // 设置默认值
                setValueByTime(paramsDto, split[0]);
                paramsDto.setStartTime(split[0]);
                paramsDto.setEndTime(split[1]);
            } else {
                // 如果时间间距为空，则按照默认值来获取时间:按照天维度,西安市月初到现在的数据，以天维度获取
                String timeInterval = TimeFormatConverter.generateRangeTime("");
                String[] split = timeInterval.split("至");
                // 设置默认值:如果开始时间和结束时间，单位都为空的话，则默认按照天维度,西安市月初到现在的数据，以天维度获取
                setValueByTime(paramsDto, split[0].substring(0, 7));
                paramsDto.setStartTime(split[0]);
                paramsDto.setEndTime(split[1]);
            }
        }
    }

//    public static ImmutablePair<String, String> parseParams(String startTime, String endTime) {
//        // 时间都不为空
//        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
//            String time = startTime;
//            // 判断时间格式
//            startTime = TimeFormatConverter.convertToFullFormat(startTime, 0);
//            endTime = TimeFormatConverter.convertToFullFormat(startTime, 1);
//        }
//        // 开始时间不为空
//        else if (StringUtils.isNotBlank(startTime)) {
//            // 按照时间，推断时间单位，返回时间格式等
//            String timeUnit = TimeFormatConverter.getTimeUnit(startTime);
//            // 按照开始时间，获取结束时间
//            String timeInterval = TimeFormatConverter.generateRange(timeUnit);
//            String[] split = timeInterval.split("至");
//            // 如果开始时间的格式不符合要求，则重新赋值
//            if (startTime.length() != 19) {
//                startTime = split[0];
//            }
//            endTime = split[1];
//        }
//        // 结束时间不为空
//        else if (StringUtils.isNotBlank(endTime)) {
//            // 按照结束时间，推断时间单位，返回时间格式等
//            String timeUnit = TimeFormatConverter.getTimeUnit(endTime);
//            // 按照开始时间，获取结束时间
//            String timeInterval = TimeFormatConverter.generateRange(timeUnit);
//            String[] split = timeInterval.split("至");
//            // 设置默认值:如果开始时间和结束时间，单位都为空的话，则默认按照天维度,西安市月初到现在的数据，以天维度获取
//            startTime = split[0];
//            if (endTime.length() != 19) {
//                endTime = split[1];
//            }
//        }
//        // 时间都为空
//        else {
//            // 如果时间间距为空，则按照默认值来获取时间:按照天维度,西安市月初到现在的数据，以天维度获取
//            String timeInterval = TimeFormatConverter.generateRange("D");
//            String[] split = timeInterval.split("至");
//            // 设置默认值:如果开始时间和结束时间，单位都为空的话，则默认按照天维度,西安市月初到现在的数据，以天维度获取
//            startTime = split[0];
//            endTime = split[1];
//        }
//        return ImmutablePair.of(startTime, endTime);
//    }

    /**
     * 处理开始结束日期
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static ImmutablePair<String, String> parseTimeParams(String startTime, String endTime) {
        IntervalValParamsDto paramsDto = new IntervalValParamsDto();
        paramsDto.setStartTime(startTime);
        paramsDto.setEndTime(endTime);
        parseParms(paramsDto);
        return ImmutablePair.of(paramsDto.getStartTime(), paramsDto.getEndTime());
    }

    /**
     * 按照时间的格式，来为其他空字段设置默认值
     *
     * @param paramsDto 封装参数
     * @param time      时间
     * @desc: 设置时间
     */
    private static void setValueByTime(IntervalValParamsDto paramsDto, String time) {
        // 如果步长为0，则按照空处理
        paramsDto.setTs(paramsDto.getTs() != null && paramsDto.getTs() > 0 ? paramsDto.getTs() : null);
        // 如果时间单位为空，则需要按照【时间格式】来获取时间单位
        if (StringUtils.isBlank(paramsDto.getTsUnit()) && paramsDto.getTs() != null) {
            // 按照时间格式，获取单位:默认按照开始时间获取
            String timeUnit = TimeFormatConverter.getTimeUnit(time);
            // 默认为天：D
            paramsDto.setTsUnit(timeUnit);
        }

        // 如果时间格式为空，则需要按照时间格式来获取时间单位
        if (StringUtils.isBlank(paramsDto.getFormatVal())) {
            // 按照时间格式，获取单位:默认按照开始时间获取
            String format = TimeFormatConverter.getTimeFormat(time);
            // 默认为小时
            paramsDto.setFormatVal(format);
        }

        // 如果时间格式为空，则需要按照时间格式来获取时间单位
        if (StringUtils.isBlank(paramsDto.getCalcType())) {
            // 默认为最新值
            paramsDto.setCalcType("LAST");
        }
    }
}