package com.siact.sec.utils;

import com.siact.common.constant.ConstantException;
import com.siact.common.constant.ConstantNum;
import com.siact.common.exception.CustomException;
import com.siact.sec.dto.IntervalDataDto;
import com.siact.sec.dto.SummaryDataDto;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * dataServer数据处理工具类
 * @author：GP
 * @date：2024/3/6 11:18
 */
public class DataServerUtils {


    /**
     * 用于数字孪生等时间间隔返回值时间格式化处理
     * @param dataDtoList
     * @param formatVal
     * @return
     */
    public static List<IntervalDataDto> intervalDataFormat(List<IntervalDataDto> dataDtoList, String formatVal) {
        return dataDtoList.stream().peek(d -> d.setTime(IntervalTimeUtil.dateFormat(d.getTime(), formatVal))).collect(Collectors.toList());
    }



    /**
     * 用于量费查询等时间间隔返回值时间格式化处理
     * @param dataDtoList
     * @param formatVal
     * @return
     */
    public static List<SummaryDataDto> summaryDataFormat(List<SummaryDataDto> dataDtoList, String formatVal) {
        return dataDtoList.stream().peek(d -> d.setTime(IntervalTimeUtil.dateFormat(d.getTime(), formatVal))).collect(Collectors.toList());
    }


    /**
     * dataSummary分组查询时groupLength处理类
     * @param tsUnit
     * @return
     */
    public static Integer summaryGroupLength(String tsUnit) {
        if (StringUtils.isBlank(tsUnit)) {
            throw new CustomException(ConstantException.argumentException);
        }
        switch (tsUnit) {
            case "D":
                return ConstantNum.NUMBER_TEN;
            case "M":
                return ConstantNum.NUMBER_SEVEN;
            case "Y":
                return ConstantNum.NUMBER_FOUR;
            default:
                throw new CustomException(ConstantException.argumentException);
        }
    }



}
