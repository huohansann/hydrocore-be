package com.siact.sec.utils;

import com.siact.common.constant.ConstantException;
import com.siact.common.constant.ConstantTime;
import com.siact.common.constant.ConstantUtil;
import com.siact.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * 时间间隔工具类
 *
 * @author：GP
 * @date：2024/3/5 8:52
 */
@Slf4j
public class IntervalTimeUtil {


    /**
     * 等时间截取
     *  data-summary length
     *
     * @return
     */


    /**
     * 等时间间隔时间集合
     * @param startTime 开始时间，格式yyyy-MM-dd hh:mm:ss 或yyyy-MM-dd
     * @param endTime 结束时间，格式yyyy-MM-dd hh:mm:ss 或yyyy-MM-dd
     * @param tsUnit 时间单位 Y:年;M:月;D:日;H:小时;MIN:分;S:秒
     * @param ts 时间长度
     * @param formatVal 时间格式，如hh:mm:ss
     * @return
     */
    public static List<String> queryIntervalTimeList(String startTime, String endTime, String tsUnit, Integer ts, String formatVal) {
        try {
            if (StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime) || StringUtils.isBlank(tsUnit) || ObjectUtils.isEmpty(ts) || StringUtils.isBlank(formatVal)) {
                throw new CustomException(ConstantException.argumentException);
            }

            List<String> timeList = new ArrayList<>();

            DateTimeFormatter dfDateTime = DateTimeFormatter.ofPattern(formatVal, Locale.CHINA);
            AtomicBoolean flag = new AtomicBoolean(true);
            // 时间格式为yyyy-MM-dd
            if (startTime.length() == 10 && endTime.length() == 10) {
                LocalDate sDate = LocalDate.parse(startTime, ConstantUtil.DATE_FORMATTER);
                LocalDate eDate = LocalDate.parse(endTime, ConstantUtil.DATE_FORMATTER);
                while (sDate.isBefore(eDate) || sDate.isEqual(eDate)) {
                    // 加第一个
                    if (flag.get()) {
                        timeList.add(sDate.format(dfDateTime));
                    }
                    flag.set(false);
                    switch (tsUnit) {
                        case "D":
                            sDate = sDate.plusDays(ts);
                            break;
                        case "M":
                            sDate = sDate.plusMonths(ts);
                            break;
                        case "Y":
                            sDate = sDate.plusYears(ts);
                            break;
                        default:
                            throw new CustomException("传入时间参数错误!!");
                    }
                    if (sDate.isBefore(eDate)|| sDate.isEqual(eDate)) {
                        timeList.add(sDate.format(dfDateTime));
                    }
                }

            } else if (startTime.length() == 19 && endTime.length() == 19){
                LocalDateTime sDate = LocalDateTime.parse(startTime, ConstantUtil.DATE_TIME_FORMATTER);
                LocalDateTime eDate = LocalDateTime.parse(endTime, ConstantUtil.DATE_TIME_FORMATTER);
                while (sDate.isBefore(eDate)) {
                    if (flag.get()) {
                        timeList.add(sDate.format(dfDateTime));
                    }
                    flag.set(false);
                    switch (tsUnit) {
                        case "S":
                            sDate = sDate.plusSeconds(ts);
                            break;
                        case "MIN":
                            sDate = sDate.plusMinutes(ts);
                            break;
                        case "H":
                            sDate = sDate.plusHours(ts);
                            break;
                        case "D":
                            sDate = sDate.plusDays(ts);
                            break;
                        case "M":
                            sDate = sDate.plusMonths(ts);
                            break;
                        case "Y":
                            sDate = sDate.plusYears(ts);
                            break;
                        default:
                            throw new CustomException("传入时间参数错误!!");
                    }
                    if (sDate.isBefore(eDate)) {
                        timeList.add(sDate.format(dfDateTime));
                    }
                }
            } else {
                throw new CustomException(ConstantException.timeException);
            }
            return timeList;
        } catch (Exception e) {
            log.error("等时间间隔时间截取出错--->", e);
            throw new CustomException(ConstantException.timeException);
        }
    }


    /**
     * 时间格式化
     *  支持格式：yyyy、yyyy-MM、yyyy-MM-dd、yyyy-MM-dd hh:mm:ss
     * @param time
     * @param formatVal
     * @return
     */
    public static String dateFormat(String time, String formatVal) {
        try {
            if (time.length() == 4 || time.length() == 7) {
                // 时间格式为yyyy或yyyy-MM
                String pattern = time.length() == 4 ? ConstantTime.YEAR_FORMAT : ConstantTime.MONTH_FORMAT;
                SimpleDateFormat inputFormat = new SimpleDateFormat(pattern);
                Date date = inputFormat.parse(time);
                return new SimpleDateFormat(formatVal).format(date);
           } else if (time.length() == 10) {
                // 时间格式为yyyy-MM-dd
                return LocalDate.parse(time).format(DateTimeFormatter.ofPattern(formatVal));
            } else if (time.length() == 19) {
                // 时间格式为yyyy-MM-dd hh:mm:ss
                return LocalDateTime.parse(time, ConstantUtil.DATE_TIME_FORMATTER).format(DateTimeFormatter.ofPattern(formatVal));
            } else {
                throw new CustomException("时间格式化异常");
            }
        }catch (Exception e) {
            log.error("时间格式化出错--->", e);
            throw new CustomException(ConstantException.timeException);
        }
    }





}
