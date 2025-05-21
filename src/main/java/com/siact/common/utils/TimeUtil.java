package com.siact.common.utils;

import com.alibaba.fastjson2.JSONObject;
import com.siact.common.constant.ConstantDataApi;
import com.siact.common.constant.ConstantNum;
import com.siact.common.constant.ConstantTime;
import com.siact.common.constant.ConstantUtil;
import com.siact.common.exception.ActiveException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author dell
 */

public class TimeUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(TimeUtil.class);

    public static DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //============================借助Calendar类获取今天、昨天、本周、上周、本年及特定时间的开始时间和结束时间（返回类型为date类型）========================

    /**
     * 获取当前时间
     * @return
     */
    public static String getNowStr(String pattern){
        SimpleDateFormat df = new SimpleDateFormat(pattern);//设置日期格式
        return df.format(new Date());
    }

    public static String getNow(){
        LocalDateTime now = LocalDateTime.now();
        return ConstantUtil.DATE_TIME_FORMATTER.format(now);
    }

    /**
     * 获取当天开始时间
     * @return
     */
    public static Date getDayBegin(){
        Calendar cal= Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);//0点
        cal.set(Calendar.MINUTE, 0);//0分
        cal.set(Calendar.SECOND, 0);//0秒
        cal.set(Calendar.MILLISECOND, 0);//0毫秒
        return cal.getTime();
    }


    /**
     * 获取当天结束时间
     * @return
     */
    public static Date getDayEnd(){
        Calendar cal= Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);//23点
        cal.set(Calendar.MINUTE, 59);//59分
        cal.set(Calendar.SECOND, 59);//59秒
        return cal.getTime();
    }


    /**
     * 获取昨天开始时间
     * @return
     */
    public static Date getBeginDayOfYesterday(){
        Calendar cal= Calendar.getInstance();
        cal.setTime(getDayBegin());//当天开始时间
        cal.add(Calendar.DAY_OF_MONTH, -1);//当天月份天数减1
        return cal.getTime();
    }


    /**
     * 获取昨天结束时间
     * @return
     */
    public static Date getEndDayOfYesterday(){
        Calendar cal= Calendar.getInstance();
        cal.setTime(getDayEnd());//当天结束时间
        cal.add(Calendar.DAY_OF_MONTH, -1);//当天月份天数减1
        return cal.getTime();
    }


    /**
     * 获取明天开始时间
     * @return
     */
    public static Date getBeginDayOfTomorrow(){
        Calendar cal= Calendar.getInstance();
        cal.setTime(getDayBegin());//当天开始时间
        cal.add(Calendar.DAY_OF_MONTH, 1);//当天月份天数加1
        return cal.getTime();
    }


    /**
     * 获取明天结束时间
     * @return
     */
    public static Date getEndDayOfTomorrow(){
        Calendar cal= Calendar.getInstance();
        cal.setTime(getDayEnd());//当天结束时间
        cal.add(Calendar.DAY_OF_MONTH, 1);//当天月份天数加1
        return cal.getTime();
    }


    /**
     * 获取某个日期的开始时间
     * @param d
     * @return
     */
    public static Timestamp getDayStartTime(Date d) {
        Calendar calendar= Calendar.getInstance();
        if(null!=d){
            calendar.setTime(d);
        }
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Timestamp(calendar.getTimeInMillis());
    }


    /**
     * 获取某个日期的结束时间
     * @param d
     * @return
     */
    public static Timestamp getDayEndTime(Date d) {
        Calendar calendar= Calendar.getInstance();
        if(null!=d){
            calendar.setTime(d);
        }
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return new Timestamp(calendar.getTimeInMillis());
    }


    /**
     * 获取本周的开始时间
     * @return
     */
    @SuppressWarnings("unused")
    public static Date getBeginDayOfWeek(){
        Date date=new Date();
        if(date==null){
            return null;
        }
        Calendar cal= Calendar.getInstance();
        cal.setTime(date);
        int dayOfWeek=cal.get(Calendar.DAY_OF_WEEK);
        if(dayOfWeek==1){
            dayOfWeek+=7;
        }
        cal.add(Calendar.DATE, 2-dayOfWeek);
        return getDayStartTime(cal.getTime());
    }

    /**
     * 获取当前时间 24
     *
     * @return
     */
    public static String getCurrentTime24() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(new Date());
    }



    /**
     * 获取本周的结束时间
     * @return
     */
    public static Date getEndDayOfWeek(){
        Calendar cal= Calendar.getInstance();
        cal.setTime(getBeginDayOfWeek());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        Date weekEndSta = cal.getTime();
        return getDayEndTime(weekEndSta);
    }


    /**
     * 获取上周开始时间
     */
    @SuppressWarnings("unused")
    public static Date getBeginDayOfLastWeek() {
        Date date=new Date();
        if (date==null) {
            return null;
        }
        Calendar cal= Calendar.getInstance();
        cal.setTime(date);
        int dayofweek=cal.get(Calendar.DAY_OF_WEEK);
        if (dayofweek==1) {
            dayofweek+=7;
        }
        cal.add(Calendar.DATE, 2-dayofweek-7);
        return getDayStartTime(cal.getTime());
    }


    /**
     * 获取上周的结束时间
     * @return
     */
    public static Date getEndDayOfLastWeek(){
        Calendar cal= Calendar.getInstance();
        cal.setTime(getBeginDayOfLastWeek());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        Date weekEndSta = cal.getTime();
        return getDayEndTime(weekEndSta);
    }


    /**
     * 获取今年是哪一年
     * @return
     */
    public static Integer getNowYear(){
        Date date = new Date();
        GregorianCalendar gc=(GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        return Integer.valueOf(gc.get(1));
    }
    /**
     * 按月加
     *
     * @param value
     * @return
     */
    public static final Date addMonth(Date date, int value) {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
//        now.add(Calendar.MONTH, 1);
//        now.set(Calendar.DATE, value);
//        return now.getTime();
        now.add(Calendar.MONTH, value);
        return now.getTime();
    }

    /**
     * 获取本月是哪一月
     * @return
     */
    public static int getNowMonth() {
        Date date = new Date();
        GregorianCalendar gc=(GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        return gc.get(2) + 1;
    }


    /**
     * 获取本月的开始时间
     * @return
     */
    public static Date getBeginDayOfMonth() {
        Calendar calendar= Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth()-1, 1);
        return getDayStartTime(calendar.getTime());
    }


    /**
     * 获取本月的结束时间
     * @return
     */
    public static Date getEndDayOfMonth() {
        Calendar calendar= Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth()-1, 1);
        int day = calendar.getActualMaximum(5);
        calendar.set(getNowYear(), getNowMonth()-1, day);
        return getDayEndTime(calendar.getTime());
    }

    /**
     * 获取指定格式日期的月底日期字符串
     * @param startTime
     * @return
     */
    public static String getEndDayOfMonthByStr(String startTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            // 将起始时间字符串解析为Date对象
            Date date = dateFormat.parse(startTime);
            // 创建Calendar对象并设置为起始时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            // 设置为月底
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            // 获取结束时间的Date对象
            Date endTime = calendar.getTime();
            // 将结束时间转换回字符串格式
            return dateFormat.format(endTime);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 获取上月的开始时间
     * @return
     */
    public static Date getBeginDayOfLastMonth() {
        Calendar calendar= Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth()-2, 1);
        return getDayStartTime(calendar.getTime());
    }


    /**
     * 获取上月的结束时间
     * @return
     */
    public static Date getEndDayOfLastMonth() {
        Calendar calendar= Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth()-2, 1);
        int day = calendar.getActualMaximum(5);
        calendar.set(getNowYear(), getNowMonth()-2, day);
        return getDayEndTime(calendar.getTime());
    }


    /**
     * 获取本年的开始时间
     * @return
     */
    public static Date getBeginDayOfYear() {
        Calendar cal= Calendar.getInstance();
        cal.set(Calendar.YEAR, getNowYear());
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        return getDayStartTime(cal.getTime());
    }


    /**
     * 获取本年的结束时间
     * @return
     */
    public static Date getEndDayOfYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, getNowYear());
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DATE, 31);
        return getDayEndTime(cal.getTime());
    }


    /**
     * 两个日期相减得到的天数
     * @param beginDate
     * @param endDate
     * @return
     */
    public static int getDiffDays(Date beginDate, Date endDate) {
        if(beginDate==null||endDate==null) {
            throw new IllegalArgumentException("getDiffDays param is null!");
        }
        long diff=(endDate.getTime()-beginDate.getTime())/(1000*60*60*24);
        int days = new Long(diff).intValue();
        return days;
    }


    /**
     * 两个日期相减得到的毫秒数
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long dateDiff(Date beginDate, Date endDate) {
        long date1ms=beginDate.getTime();
        long date2ms=endDate.getTime();
        return date2ms-date1ms;
    }


    /**
     * 获取两个日期中的最大日起
     * @param beginDate
     * @param endDate
     * @return
     */
    public static Date max(Date beginDate, Date endDate) {
        if(beginDate==null) {
            return endDate;
        }
        if(endDate==null) {
            return beginDate;
        }
        if(beginDate.after(endDate)) {//beginDate日期大于endDate
            return beginDate;
        }
        return endDate;
    }


    /**
     * 获取两个日期中的最小日期
     * @param beginDate
     * @param endDate
     * @return
     */
    public static Date min(Date beginDate, Date endDate) {
        if(beginDate==null) {
            return endDate;
        }
        if(endDate==null) {
            return beginDate;
        }
        if(beginDate.after(endDate)) {
            return endDate;
        }
        return beginDate;
    }


    /**
     * 获取某月该季度的第一个月
     * @param date
     * @return
     */
    public static Date getFirstSeasonDate(Date date) {
        final int[] SEASON={ 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4 };
        Calendar cal= Calendar.getInstance();
        cal.setTime(date);
        int sean = SEASON[cal.get(Calendar.MONTH)];
        cal.set(Calendar.MONTH, sean*3-3);
        return cal.getTime();
    }


    /**
     * 返回某个日期下几天的日期
     * @param date
     * @param i
     * @return
     */
    public static Date getNextDay(Date date, int i) {
        Calendar cal=new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.DATE,cal.get(Calendar.DATE)+i);
        return cal.getTime();
    }


    /**
     * 返回某个日期前几天的日期
     * @param date
     * @param i
     * @return
     */
    public static Date getFrontDay(Date date, int i) {
        Calendar cal=new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.DATE, cal.get(Calendar.DATE)-i);
        return cal.getTime();
    }



    /**
     * 获取某年某月按天切片日期集合（某个月间隔多少天的日期集合）
     * @param beginYear
     * @param beginMonth
     * @param k
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static List getTimeList(int beginYear, int beginMonth, int k) {
        List list = new ArrayList();
        Calendar begincal=new GregorianCalendar(beginYear,beginMonth, 1);
        int max = begincal.getActualMaximum(Calendar.DATE);
        for (int i = 1; i < max; i = i + k) {
            list.add(begincal.getTime());
            begincal.add(Calendar.DATE, k);
        }
        begincal = new GregorianCalendar(beginYear, beginMonth, max);
        list.add(begincal.getTime());
        return list;
    }


    /**
     * 获取某年某月到某年某月按天的切片日期集合（间隔天数的集合）
     * @param beginYear
     * @param beginMonth
     * @param endYear
     * @param endMonth
     * @param k
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List getTimeList(int beginYear, int beginMonth, int endYear, int endMonth, int k) {
        List list = new ArrayList();
        if (beginYear==endYear){
            for(int j=beginMonth;j<=endMonth;j++){
                list.add(getTimeList(beginYear,j,k));
            }
        }else{
            {
                for(int j=beginMonth;j<12;j++){
                    list.add(getTimeList(beginYear,j,k));
                }
                for(int i=beginYear+1;i<endYear;i++) {
                    for (int j=0; j<12; j++) {
                        list.add(getTimeList(i,j,k));
                    }
                }
                for (int j=0;j <= endMonth; j++) {
                    list.add(getTimeList(endYear, j, k));
                }
            }
        }
        return list;
    }




    //=================================时间格式转换==========================

    /**
     * date类型进行格式化输出（返回类型：String）
     * @param date
     * @return
     */
    public static String dateFormat(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }
    public static String dateToStrDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(date);
        return dateString;
    }
    public static String dateFormatYYYYMM(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        String dateString = formatter.format(date);
        return dateString;
    }

    /**
     * 将"2015-08-31 21:08:06"型字符串转化为Date
     * @param str
     * @return
     * @throws ParseException
     */
    public static Date StringToDate(String str) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = (Date) formatter.parse(str);
        return date;
    }

    /**
     * 将"2015-08-31"型字符串转化为Date
     * @param str
     * @return
     * @throws ParseException
     */
    public static Date StringToDateYMD(String str) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = (Date) formatter.parse(str);
        return date;
    }

    /**
     * 将"2015-08-31"型字符串转化为Date
     * @param str
     * @return
     * @throws ParseException
     */
    public static Date StringToDateYM(String str) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        Date date = (Date) formatter.parse(str);
        return date;
    }
    /**
     * 获取2020-05-25 20:42:13 隔12个月以后的日期  2021-05-25 20:42:13
     * @param date
     * @param month
     * @return
     */
    public static String parseSpaceDate(Date date, int month){
        Calendar c = Calendar.getInstance();
        c.setTime(date);   //设置时间
        c.add(Calendar.MONTH, month); //日期分钟加1,Calendar.DATE(天),Calendar.HOUR(小时)
        Date date1 = c.getTime(); //结果
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date1);
        return  dateString;
    }


    /**
     * 将CST时间类型字符串进行格式化输出
     * @param str
     * @return
     * @throws ParseException
     */
    public static String CSTFormat(String str) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Date date = (Date) formatter.parse(str);
        return dateFormat(date);
    }



    /**
     * 将long类型转化为Date
     * @param str
     * @return
     * @throws ParseException
     */
    public static Date LongToDate(long str) throws ParseException {
        return new Date(str * 1000);
    }




    //====================================其他常见日期操作方法======================

    /**
     * 判断当前日期是否在[startDate, endDate]区间
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @author jqlin
     * @return
     */
    public static boolean isEffectiveDate(Date startDate, Date endDate){
        if(startDate == null || endDate == null){
            return false;
        }
//        long currentTime = new Date().getTime();
        long currentTime = System.currentTimeMillis();
        if(currentTime >= startDate.getTime()
                && currentTime <= endDate.getTime()){
            return true;
        }
        return false;
    }


    /**
     * 得到二个日期间的间隔天数
     * @param secondString：后一个日期
     * @param firstString：前一个日期
     * @return
     */
    public static String getTwoDay(String secondString, String firstString) {
        SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
        long day = 0;
        try {
            Date secondTime = myFormatter.parse(secondString);
            Date firstTime = myFormatter.parse(firstString);
            day = (secondTime.getTime() - firstTime.getTime()) / (24 * 60 * 60 * 1000);
        } catch (Exception e) {
            return "";
        }
        return day + "";
    }


    /**
     * 时间前推或后推分钟,其中JJ表示分钟.
     * @param StringTime：时间
     * @param minute：分钟（有正负之分）
     * @return
     */
    public static String getPreTime(String StringTime, String minute) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String mydate1 = "";
        try {
            Date date1 = format.parse(StringTime);
            long Time = (date1.getTime() / 1000) + Integer.parseInt(minute) * 60;
            date1.setTime(Time * 1000);
            mydate1 = format.format(date1);
        } catch (Exception e) {
            return "";
        }
        return mydate1;
    }

    /**
     * 时间前推或后推秒,其中JJ表示秒.
     * @param StringTime：时间
     * @param second：秒（有正负之分）
     * @return
     */
    public static String getPreTimeBySecond(String StringTime, String second) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String mydate1 = "";
        try {
            Date date1 = format.parse(StringTime);
            long Time = (date1.getTime() / 1000) + Integer.parseInt(second) ;
            date1.setTime(Time * 1000);
            mydate1 = format.format(date1);
        } catch (Exception e) {
            return "";
        }
        return mydate1;
    }


    /**
     * 将短时间格式字符串转换为时间 yyyy-MM-dd
     *
     * @param strDate
     * @return
     */
    public static Date strToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    public static Date strToDateYM(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }
    /**
     * 将短时间格式字符串转换为时间 yyyy-MM-dd
     *
     * @param strDate
     * @return
     */
    public static Date strToDateFormat(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }



    /**
     * 得到一个时间延后或前移几天的时间
     * @param nowdate：时间
     * @param delay：前移或后延的天数
     * @return
     */
    public static String getNextDay(String nowdate, String delay) {
        try{
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String mdate = "";
            Date d = strToDate(nowdate);
            long myTime = (d.getTime() / 1000) + Integer.parseInt(delay) * 24 * 60 * 60;
            d.setTime(myTime * 1000);
            mdate = format.format(d);
            return mdate;
        }catch(Exception e){
            return "";
        }
    }


    /**
     * 判断是否闰年
     * @param ddate
     * @return
     */
    public static boolean isLeapYear(String ddate) {
        /**
         * 详细设计： 1.被400整除是闰年，否则： 2.不能被4整除则不是闰年 3.能被4整除同时不能被100整除则是闰年
         * 3.能被4整除同时能被100整除则不是闰年
         */
        Date d = strToDate(ddate);
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(d);
        int year = gc.get(Calendar.YEAR);
        if ((year % 400) == 0){
            return true;
        }else if ((year % 4) == 0){
            if ((year % 100) == 0){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }


    /**
     * 返回美国时间格式
     * @param str
     * @return
     */
    public static String getEDate(String str) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(str, pos);
        String j = strtodate.toString();
        String[] k = j.split(" ");
        return k[2] + k[1].toUpperCase() + k[5].substring(2, 4);
    }


    /**
     * 判断二个时间是否在同一个周
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameWeekDates(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
        if(0 == subYear) {
            if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)){
                return true;
            }
        }else if(1 == subYear && 11 == cal2.get(Calendar.MONTH)) {
            // 如果12月的最后一周横跨来年第一周的话则最后一周即算做来年的第一周
            if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)){
                return true;
            }
        }else if (-1 == subYear && 11 == cal1.get(Calendar.MONTH)) {
            if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)){
                return true;
            }
        }
        return false;
    }



    /**
     * 产生周序列,即得到当前时间所在的年度是第几周
     * @return
     */
    public static String getSeqWeek() {
        Calendar c = Calendar.getInstance(Locale.CHINA);
        String week = Integer.toString(c.get(Calendar.WEEK_OF_YEAR));
        if (week.length() == 1) {
            week = "0" + week;
        }
        String year = Integer.toString(c.get(Calendar.YEAR));
        return year +"年第"+ week+"周";
    }


    /**
     * 获得一个日期所在的周的星期几的日期，如要找出2002年2月3日所在周的星期一是几号
     * @param sdate：日期
     * @param num：星期几（星期天是一周的第一天）
     * @return
     */
    public static String getWeek(String sdate, String num) {
        // 再转换为时间
        Date dd = strToDate(sdate);
        Calendar c = Calendar.getInstance();
        c.setTime(dd);
        if ("1".equals(num)) // 返回星期一所在的日期
        {
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        } else if ("2".equals(num)) // 返回星期二所在的日期
        {
            c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        } else if ("3".equals(num)) // 返回星期三所在的日期
        {
            c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        } else if ("4".equals(num)) // 返回星期四所在的日期
        {
            c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        } else if ("5".equals(num)) // 返回星期五所在的日期
        {
            c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        } else if ("6".equals(num)) // 返回星期六所在的日期
        {
            c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        } else if ("0".equals(num)) // 返回星期日所在的日期
        {
            c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }


    /**
     * 根据一个日期，返回是星期几的字符串
     * @param sdate
     * @return
     */
    public static String getWeek(String sdate) {
        // 再转换为时间
        Date date = strToDate(sdate);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        // int hour=c.get(Calendar.DAY_OF_WEEK);
        // hour中存的就是星期几了，其范围 1~7
        // 1=星期日 7=星期六，其他类推
        return new SimpleDateFormat("EEEE").format(c.getTime());
    }

    /**
     * 得到具体的某个星期几 比如2020-06-20 实际上市礼拜6 得到的就是6
     * @param sdate
     * @return
     */
    public static String getFormatWeeker(String sdate){
        Date date = strToDate(sdate);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int week =c.get(Calendar.DAY_OF_WEEK);
        String weekStr="";
        if(1 == week ){
            weekStr = "7";
        }else if( 2 == week){
            weekStr = "1";
        }else if(3 == week){
            weekStr = "2";
        }else if(4 == week){
            weekStr = "3";
        }else if(5 == week){
            weekStr = "4";
        }else if(6 == week){
            weekStr = "5";
        }else if(7 == week){
            weekStr = "6";
        }
        return  weekStr;

    }


    /**
     * 根据一个日期，返回是星期几的字符串
     * @param sdate
     * @return
     */
    public static String getWeekStr(String sdate){
        String str = "";
        str = getWeek(sdate);
        if("1".equals(str)){
            str = "星期日";
        }else if("2".equals(str)){
            str = "星期一";
        }else if("3".equals(str)){
            str = "星期二";
        }else if("4".equals(str)){
            str = "星期三";
        }else if("5".equals(str)){
            str = "星期四";
        }else if("6".equals(str)){
            str = "星期五";
        }else if("7".equals(str)){
            str = "星期六";
        }
        return str;
    }


    /**
     * 两个时间之间的天数
     * @param date1
     * @param date2
     * @return
     */
    public static long getDays(String date1, String date2) {
        if (date1 == null || "".equals(date1)) {
            return 0;
        }
        if (date2 == null || "".equals(date2)) {
            return 0;
        }
        // 转换为标准时间
        SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        Date mydate = null;
        try {
            date = myFormatter.parse(date1);
            mydate = myFormatter.parse(date2);
        } catch (Exception e) {
        }
        long day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
        return day;
    }


    /**
     * 形成如下的日历 ， 根据传入的一个时间返回一个结构 星期日 星期一 星期二 星期三 星期四 星期五 星期六 下面是当月的各个时间
     * 此函数返回该日历第一行星期日所在的日期
     * @param sdate
     * @return
     */
    public static String getNowMonth(String sdate) {
        // 取该时间所在月的一号
        sdate = sdate.substring(0, 8) + "01";

        // 得到这个月的1号是星期几
        Date date = strToDate(sdate);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int u = c.get(Calendar.DAY_OF_WEEK);
        String newday = getNextDay(sdate, (1 - u) + "");
        return newday;
    }


    /**
     * 根据用户传入的时间表示格式以及时间，返回用户想要的时间格式
     * @param type
     * @return
     */
    public static String getUserDate(String type, String time) {
        String dateString = null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = formatter.parse(time);
            SimpleDateFormat format = new SimpleDateFormat(type);
            dateString = format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }


    /**
     * 返回一个i位数的随机数
     * @param i
     * @return
     */
    public static String getRandom(int i) {
        Random jjj = new Random();
        // int suiJiShu = jjj.nextInt(9);
        if (i == 0) {
            return "";
        }
        String jj = "";
        for (int k = 0; k < i; k++) {
            jj = jj + jjj.nextInt(9);
        }
        return jj;
    }


    /**
     * 取得数据库主键 生成格式为yyyymmddhhmmss+k位随机数
     * @param k：表示是取几位随机数，可以自己定
     * @return
     */
    public static String getNo(int k) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return getUserDate("yyyyMMddhhmmss", formatter.format(new Date())) + getRandom(k);
    }
    /**
     * 获取本日
     * @return
     */
    public static String getDay() {
        return LocalDate.now().toString();
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//        Date date = new Date();
//        return formatter.format(date);
    }
    /**
     * 获取本月
     * @return
     */
    public static String getMonth() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        Date date = new Date();
        return formatter.format(date);
    }

    /**
     * 获取本年
     * @return
     */
    public static String getYear() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        Date date = new Date();
        return formatter.format(date);
    }

    /**
     * 当前月往前推几个月
     * @return
     */
    public static String getPreMonth(int num) {
        SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date); // 设置为当前时间
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - num); // 设置为上一个月
        date = calendar.getTime();
        return format.format(date);
    }

    /**
     * 指定月往前推几个月
     * @return
     */
    @SneakyThrows
    public static String getAppointMonth(String time, int num, String pattern) {
        if (time.length() == 7) {
            time = time + "-01 00:00:00";
        } else if (time.length() == 10) {
            time = time + " 00:00:00";
        }
//        SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM");
        SimpleDateFormat format  = new SimpleDateFormat(pattern);
        Date date = StringToDate(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date); // 设置为当前时间
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - num); // 设置为上一个月
        date = calendar.getTime();
        return format.format(date);
    }

    /**
     * 指定月往后推几个月
     * @return
     */
    @SneakyThrows
    public static String getAppointsMonth(String time, int num, String pattern) {
        if (time.length() == 7) {
            time = time + "-01 00:00:00";
        } else if (time.length() == 10) {
            time = time + " 00:00:00";
        }
//        SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM");
        SimpleDateFormat format  = new SimpleDateFormat(pattern);
        Date date = StringToDate(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date); // 设置为当前时间
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + num); // 设置为上一个月
        date = calendar.getTime();
        return format.format(date);
    }


    /**
     * 当前月往前推几个月
     * @return
     */
    public static String getPreMonthFormat(int num) {
        SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date); // 设置为当前时间
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - num); // 设置为上一个月
        date = calendar.getTime();
        return format.format(date);
    }


    public static String quarterStart(String startDate) {
        Date dBegin = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            dBegin = sdf.parse(startDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(dBegin);
        int remainder = calBegin.get(Calendar.MONTH)  % 3;
        int month = remainder != 0 ? calBegin.get(Calendar.MONTH) - remainder: calBegin.get(Calendar.MONTH);

        calBegin.set(Calendar.MONTH, month);
        calBegin.set(Calendar.DAY_OF_MONTH, calBegin.getActualMinimum(Calendar.DAY_OF_MONTH));

        calBegin.setTime(calBegin.getTime());
        return sdf.format(calBegin.getTime());
    }



    /*
     * @description: 获取当前时间所属季度结束月最后一天
     * @param: endDate
     * @return: endDate
     * @author: zhoushupeng
     * @date: 19/7/5 上午11:18
     */
    public static String quarterEnd(String endDate) {
        Date dEnd = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            dEnd = sdf.parse(endDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dEnd);
        int remainder = (calendar.get(Calendar.MONTH) + 1) % 3;
        int month = remainder != 0 ? calendar.get(Calendar.MONTH) + (3 - remainder) : calendar.get(Calendar.MONTH);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.setTime(calendar.getTime());
        return sdf.format(calendar.getTime());
    }


    // 返回时间格式如：2020-02-17 00:00:00
    public static String getStartOfDay(String time) {
        Date dTime = strToDate(time);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(dTime);
        //一天的开始时间 yyyy:MM:dd 00:00:00
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        Date dayStart = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startStr = simpleDateFormat.format(dayStart);
        return  startStr;
    }
    public static String getStartOfDayYM(String time) {
        Date dTime = strToDateYM(time);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(dTime);
        //一天的开始时间 yyyy:MM:dd 00:00:00
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        Date dayStart = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startStr = simpleDateFormat.format(dayStart);
        return  startStr;
    }

    // 返回时间格式如：2020-02-19 23:59:59
    public static String getEndOfDay(String time) {
        Date dTime = strToDate(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dTime);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }
    public static String getEndOfDayYM(String time) {
        Date dTime = strToDateYM(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dTime);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }

    public static String getLastDayOfMonth(String yearMonth) {
        int year = Integer.parseInt(yearMonth.split("-")[0]);  //年
        int month = Integer.parseInt(yearMonth.split("-")[1]); //月
        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        // cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.MONTH, month); //设置当前月的上一个月
        // 获取某月最大天数
        //int lastDay = cal.getActualMaximum(Calendar.DATE);
        int lastDay = cal.getMinimum(Calendar.DATE); //获取月份中的最小值，即第一天
        // 设置日历中月份的最大天数
        //cal.set(Calendar.DAY_OF_MONTH, lastDay);
        cal.set(Calendar.DAY_OF_MONTH, lastDay - 1); //上月的第一天减去1就是当月的最后一天
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    /**
     * 得到两个时间之间的相隔月份数
     * @param start
     * @param end
     * @return
     */
    public static int getDataDifferenceByMonth(Date start, Date end) {
        if (start.after(end)) {
            Date t = start;
            start = end;
            end = t;
        }
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(end);
        Calendar temp = Calendar.getInstance();
        temp.setTime(end);
        temp.add(Calendar.DATE, 1);

        int year = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int month = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);

        if ((startCalendar.get(Calendar.DATE) == 1) && (temp.get(Calendar.DATE) == 1)) {
            return year * 12 + month + 1;
        } else if ((startCalendar.get(Calendar.DATE) != 1) && (temp.get(Calendar.DATE) == 1)) {
            return year * 12 + month;
        } else if ((startCalendar.get(Calendar.DATE) == 1) && (temp.get(Calendar.DATE) != 1)) {
            return year * 12 + month;
        } else {
            return (year * 12 + month - 1) < 0 ? 0 : (year * 12 + month);
        }
    }

    /**
     * 获取指定月的天数集合
     * @param time
     * @return
     */
    @SneakyThrows
    public static List<String> getMonthFullDay(String time) {
        List<String> list = new ArrayList();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            Date date = sdf.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 1; i <= day; i++) {
                list.add(i+"");
            }
        }catch (Exception e){
            LOGGER.error("获取指定月的天数集合--->日期转换出错");
            return list;
        }
        return list;
    }

    /**
     * 获取指定月的天数集合，带汉子：如1日
     * @param time
     * @return
     */
    @SneakyThrows
    public static List<String> getMonthFullDayRi(String time) {
        List<String> list = new ArrayList();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            Date date = sdf.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 1; i <= day; i++) {
                list.add(i+"日");
            }
        }catch (Exception e){
            return null;
        }
        return list;
    }

    /**
     * 获取小时的天数集合
     * @return
     */
    public static List<String> getHourFullDay() {
        List<String> list = new ArrayList();
        for (int i = 0; i <= 23; i++) {
            list.add(i+"");
        }
        return list;
    }

    /**
     * 获取月份集合
     * @return
     */
    public static List<String> getdayFullMonth() {
        List<String> list = new ArrayList();
        for (int i = 1; i <= 12; i++) {
            list.add(i+"");
        }
        return list;
    }

    /**
     * 获取月份集合
     * @return
     */
    public static List<String> getFullMonth() {
        List<String> list = new ArrayList();
        for (int i = 1; i <= 12; i++) {
            if(i<= 9){
                list.add("0" + i);
            }else{
                list.add(i+"");
            }
        }
        return list;
    }

    /**
     * 获取小时的天数集合  得到 标准的 00:00, 01:00, 02:00, 03:00, 04:00, 05:00, 06:00
     * @return
     */
    public static List<String> getHourFullDayFormat(){
        List<String> list = new ArrayList();
        for (int i = 0; i <= 23; i++) {
            if(i<= 9){
                list.add("0" + i +":00");
            }else{
                list.add(i+":00");
            }
        }
        return list;
    }
    /**
     * 获取小时的天数集合  得到 标准的 00:00, 01:00, 02:00, 03:00, 04:00, 05:00, 06:00
     * @return
     */
    public static List<String> getHourFullDayFormatAll(){
        List<String> list = new ArrayList();
        for (int i = 1; i <= 24; i++) {
            if(i<= 9){
                list.add("0" + i +":00");
            }else{
                list.add(i+":00");
            }
        }
        return list;
    }


    /**
     * 获取指定月的天数集合(返回yyyy-MM-dd)
     * @param time
     * @return
     */
    @SneakyThrows
    public static List<String> getMonthAndDay(String time) {
        List<String> list = new ArrayList();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            Date date = sdf.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 1; i <= day; i++) {
                list.add(time.substring(5,7)+"月"+i+"日");
            }
        }catch (Exception e){
            return null;
        }
        return list;
    }

    /**
     * 获取指定月份内所有的天的集合 如 2020-07-01  2020-07-02 2020-07-03 2020-07-04
     * @param time
     * @return
     */
    public static List<String> getMonthAndDayYYYYMMDD(String time) {
        List<String> list = new ArrayList();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            Date date = sdf.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 1; i <= day; i++) {
                if (i<= 9){
                    list.add(time +"-0" + i);
                }else{
                    list.add(time +"-" + i);
                }
            }
        }catch (Exception e){
            LOGGER.error("获取指定月份内所有的天的集合--->日期转换出错");
            return list;
        }
        return list;
    }
    /**
     * 获取指定年份内所有的月的集合 如 2020-01, 2020-02, 2020-03, 2020-04, 2020-05, 2020-06, 2020-07, 2020-08, 2020-09, 2020-10, 2020-11, 2020-12
     * @param time
     * @return
     */
    public static List<String> getMonthAndDayYYYYMM(String time) {
        List<String> list = new ArrayList();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            Date date = sdf.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int day = calendar.getActualMaximum(Calendar.MONTH);
            for (int i = 1; i <= day+1; i++) {
                if (i<= 9){
                    list.add(time +"-0" + i);
                }else{
                    list.add(time +"-" + i);
                }
            }
        }catch (Exception e){
            return null;
        }
        return list;
    }



    @SneakyThrows
    public static List<String> getMonthAndDay2(String time) {
        List<String> list = new ArrayList();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Date date = sdf.parse(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= day; i++) {
            list.add(i+"日");
        }
        return list;
    }

    /**
     * 一天间隔15分钟集合
     * @return
     */
    public static List<String> getTimeLag5Minute() {
        ArrayList<String> list = new ArrayList<String>();//创建集合存储所有时间点
        for (int h = 0, m = 0; h < 24; m += 15) {//创建循环，指定间隔五分钟
            if (m >= 60) {//判断分钟累计到60时清零，小时+1
                h++;
                m = 0;
            }
            if (h >= 24) {//判断小时累计到24时跳出循环，不添加到集合
                break;
            }

            /*转换为字符串*/
            String hour = String.valueOf(h);
            String minute = String.valueOf(m);

            /*判断如果为个位数则在前面拼接‘0’*/
            if (hour.length() < 2) {
                hour = "0" + hour;
            }
            if (minute.length() < 2) {
                minute = "0" + minute;
            }
            list.add(hour + ":" + minute);//拼接为HH:mm格式，添加到集合
        }

        return list;
    }

    /**
     * 得到两个时间相差的分钟数 再除以24*60得到天数
     * @param beginTime
     * @param endTime
     * @return
     * @throws Exception
     */
    public static BigDecimal getDayDiff(String beginTime , String endTime)  throws Exception {
        SimpleDateFormat myFormatter = new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss");
        Date date= myFormatter.parse(endTime );
        Date mydate= myFormatter.parse( beginTime);
        long min =(date.getTime()-mydate.getTime())/(60*1000);
        System.out.println(min);
        BigDecimal minBig = new BigDecimal(Long.toString(min));
        BigDecimal indexBig = new BigDecimal(Long.toString(60*24));
        BigDecimal divide = minBig.divide(indexBig, 4, RoundingMode.HALF_UP);
        return   divide;
    }

    public static String get15MinClose(String time) {
        if(time.compareTo("45")>=0) {
            return "45";
        }else if(time.compareTo("30")>=0) {
            return "30";
        }else if(time.compareTo("15")>=0) {
            return "15";
        }else {
            return "00";
        }
    }

    /**
     * 获取当前是几号，如果传入日期小于当前时间直接为32
     * @param pattern
     * @return
     */
    public static String getNowStrDay(String pattern){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");//设置日期格式
        String nowformat = df.format(new Date());
        if (pattern.compareTo(nowformat)<0){
            return "31";
        }else {
            df = new SimpleDateFormat("dd");
            if(df.format(new Date()).startsWith("0")){
                return df.format(new Date()).substring(1,2);
            }
            return df.format(new Date());
        }
    }
    /**
     * 获取当前是几号
     * @return
     */
    public static String getNowDay(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        return df.format(new Date());
    }

    public static List<String> getTimeSlot(String start, String end){
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
        List<String> timeList = new ArrayList<String>();

        try{
            Calendar startTime = Calendar.getInstance();
            startTime.setTime(formatDate.parse(start));
            startTime.add(Calendar.DATE, -1);

            while(true){
                startTime.add(Calendar.DATE, 1);
                Date newTime = startTime.getTime();
                String newEnd=formatDate.format(newTime);
                timeList.add(newEnd);
                if(end.equals(newEnd)){
                    break;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return timeList;
    }

    /**
     * 获取从当前开始前24小时内前N个节点数组
     * 例：当前17点
     * 结果：[18:00, 19:00, 20:00, 21:00, 22:00, 23:00, 00:00, 01:00, 02:00, 03:00, 04:00, 05:00, 06:00, 07:00, 08:00, 09:00, 10:00, 11:00, 12:00, 13:00, 14:00, 15:00, 16:00, 17:00]
     * @param length
     * @return
     */
    public static List<String> getTimeList(int length){
        List<String> list = new ArrayList<>();
        if (length <= 24){
            LocalDateTime now = LocalDateTime.now();
            int start = (now.getHour() - length) > 0 ? now.getHour() - length : 24 + (now.getHour() - length);
            for(int i = 0; i < length; i++){
                start += 1;
                if (start >= 24){
                    start = start - 24;
                }
                if (start >=0 && start < 10){
                    list.add("0" + start + ":00");
                } else {
                    list.add(start + ":00");
                }
            }
        }
        return list;
    }

    // 获取去年的日期
    public static String lastYeatDay(Date nowDay, String type) {
        SimpleDateFormat format = new SimpleDateFormat(type);
        Calendar c = Calendar.getInstance();
        c.setTime(nowDay);
        c.add(Calendar.YEAR, -1);
        Date y = c.getTime();
        String year = format.format(y);
        return year;
    }

    // 获取指定的两个时间日期之间的时间数组
    public static List<String> daytoday(String sDay, String eDay) {
        ArrayList<String> dates = new ArrayList<>();
        try {
            Date sDate = new SimpleDateFormat("yyyy-MM-dd").parse(sDay);
            Date eDate = new SimpleDateFormat("yyyy-MM-dd").parse(eDay);
            // 判断是否到结束日期
            Calendar dd = Calendar.getInstance();
            dd.setTime(sDate);
            while (TimeUtil.getDiffDays(dd.getTime(), eDate) > -1){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String str = sdf.format(dd.getTime());
                dates.add(str);
                dd.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (ParseException e) {
            LOGGER.error("daytoday--->日期转换失败");
        }

        return dates;
    }

    /**
     *  计算两个时间之间的年数
     * @param time 开始时间
     * @param endTime 结束时间
     * @return  两个时间之间的年数
     */
    public static int getDiffYears(String time, String endTime) {
        Integer year = Integer.valueOf(endTime.substring(0, 4));
        Integer timeYear = Integer.valueOf(time.substring(0, 4));

        return year - timeYear + 1;
    }

    /**
     * 获取指定月份间隔数组，日期间以separator参数拼接
     * @param start
     * @param end
     * @param separator
     * @return
     */
    public static List<String> monthArr(String start, String end, String separator){
        List<String> list = new ArrayList<>();
        LocalDate sTime = LocalDate.parse(start + "-01");
        LocalDate eTime = LocalDate.parse(end + "-01");
        if (eTime.isAfter(sTime) || eTime.isEqual(sTime)){
            long until = sTime.until(eTime, ChronoUnit.MONTHS) + 1;
            for (long i = 0; i < until; i++){
                LocalDate date = sTime.plusMonths(i);
                list.add(date.toString().split("-")[0] + separator + date.toString().split("-")[1]);
            }
        }
        return list;
    }

    /**
     * 日期判断
     * @param time
     * @return
     */
    public static boolean dataVerify(String time){
        LocalDate now = LocalDate.parse(LocalDate.now().toString().substring(0,7) + "-01");
        if (!time.isEmpty()){
            String[] split = time.split("-");
            if (split.length == 1){
                int year = now.getYear();
                int v = Integer.parseInt(split[0]);
                return year > v;
            } else if (split.length == 2){
                LocalDate parse = LocalDate.parse(time + "-01");
                return now.isAfter(parse);
            } else {
                now = LocalDate.now();
                LocalDate parse = LocalDate.parse(time);
                return now.isAfter(parse);
            }
        }
        return false;
    }

    /**
     * 月份/天数小于10补零
     * @param data 月份/天数
     * @return String
     */
    public static String dateConvert(int data){
        if (data < 10){
            return "0" + data;
        } else {
            return String.valueOf(data);
        }
    }

    /**
     * 获取两个时间差值 小时列表 yyyy-MM-dd HH:mm:ss
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @return List
     */
    public static List<String> timeDiffHour(String startTime, String endTime){
        List<String> timeList = new ArrayList<>();
        LocalDateTime sTime = LocalDateTime.parse(startTime, df);
        LocalDateTime eTime = LocalDateTime.parse(endTime, df);
        long durHour = Duration.between(sTime, eTime).toHours();
        for (long i = 0; i <= durHour; i++) {
            timeList.add(sTime.plusHours(i).format(df));
        }
        return timeList;
    }

    /**
     * 获取供暖季开始计算的年
     * @return
     */
    public static String getHeatingYear(String currentYear, String startDay) throws ParseException {
        String newTime = currentYear + "-" + startDay;
        Date newTimeDate = TimeUtil.StringToDateYMD(newTime);
        Date currentDate = new Date();
        if (currentDate.getTime() < newTimeDate.getTime()) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy");
            Calendar c = Calendar.getInstance();
            c.setTime(currentDate);
            c.add(Calendar.YEAR, -1);
            Date y = c.getTime();
            return format.format(y);
        }
        else {
            return currentYear;
        }
    }

    public static String getYearMonth(String time){
        return time.substring(0, 7);
    }

    /**
     * 获取指定年月的开始时间
     */
    public static String getBeginTime(String time) {
        String[] timeArray = getYearMonth(time).split("-");
        YearMonth yearMonth = YearMonth.of(Integer.parseInt(timeArray[0]), Integer.parseInt(timeArray[1]));
        LocalDate localDate = yearMonth.atDay(1);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        ZonedDateTime zonedDateTime = startOfDay.atZone(ZoneId.of("Asia/Shanghai"));

        return sdf.format(Date.from(zonedDateTime.toInstant()));
    }

    /**
     * 获取指定年月的结束时间
     */
    public static String getEndTime(String time) {
        String[] timeArray = getYearMonth(time).split("-");
        YearMonth yearMonth = YearMonth.of(Integer.parseInt(timeArray[0]), Integer.parseInt(timeArray[1]));
        LocalDate endOfMonth = yearMonth.atEndOfMonth();
        LocalDateTime localDateTime = endOfMonth.atTime(23, 59, 59, 999);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Shanghai"));
        return sdf.format(Date.from(zonedDateTime.toInstant()));
    }

    /**
     * 根据周的起始日期列出周的集合
     */
    public static List<String> getWeekDayByFirst(String day){
        List<String> list = new ArrayList<>();
        list.add(day);
        LocalDate localDate = LocalDate.parse(day, ConstantUtil.DATE_FORMATTER);
        for(int i=1; i<7; i++){
            list.add(localDate.plusDays(i).format(ConstantUtil.DATE_FORMATTER));
        }
        return list;
    }

    /**
     * 获取指定年-月-日的开始时间
     */
    public static String getStartTimeByDay(String day){
        return day + " 00:00:00";
    }

    /**
     * 获取指定年的开始时间
     */
    public static String getStartTimeByYear(String year){
        return year + "-01-01 00:00:00";
    }

    /**
     * 获取指定年-月-日的结束时间
     */
    public static String getEndTimeByDay(String day){
        return day + " 23:59:59";
    }

    /**
     * 获取指定年的结束时间
     */
    public static String getEndTimeByYear(String year){
        return year + "-12-31 23:59:59";
    }

    /**
     * 根据时间类型不同返回传入时间  time 返回下一级list(例如: 传年2021 返回 月的list)
     */
    public static List<String> getTimeListByType(String type, String time){
        List<String> list = new ArrayList<>();
        if(StringUtils.equals(ConstantTime.YEAR, type)){
            list = getMonthAndDayYYYYMM(time);
        }else if(StringUtils.equals(ConstantTime.MONTH, type)){
            list = getMonthAndDayYYYYMMDD(time);
        }else if(StringUtils.equals(ConstantTime.WEEK, type)){
            list = getWeekDayByFirst(time);
        }else{
            LOGGER.error("getTimeListByType--->所传参数和所列不匹配");
        }
        return list;
    }

    /**
     * 根据时间类型不同返回传入时间  time 返回下一级list(例如: 传年2021 返回 月的list)
     */
    public static String getXAxisItem(String type, String time){
        String str = "";
        if(StringUtils.equals(ConstantTime.YEAR, type)){
            str = time.substring(5,7);
        }else if(StringUtils.equals(ConstantTime.MONTH, type)){
            str = time.substring(8,10);
        }else if(StringUtils.equals(ConstantTime.WEEK, type)){
            str = getWeekStr(time);
        }else{
            LOGGER.error("getXAxisItem--->所传参数和所列不匹配");
        }
        return str;
    }

    /**
     * 根据时间类型不同返回传入时间  time 的开始时间
     */
    public static String getStartTimeByType(String type, String time){
        String str = "";
        if(StringUtils.equals(ConstantTime.YEAR, type)){
            str = getBeginTime(time);
        }else if(StringUtils.equals(ConstantTime.MONTH, type)){
            str = getStartTimeByDay(time);
        }else if(StringUtils.equals(ConstantTime.WEEK, type)){
            str = getStartTimeByDay(time);
        }else{
            LOGGER.error("getStartTimeByType--->所传参数和所列不匹配");
        }
        return str;
    }

    /**
     * 根据时间类型不同返回传入时间  time 的结束时间
     */
    public static String getEndTimeByType(String type, String time){
        String str = "";
        if(StringUtils.equals(ConstantTime.YEAR, type)){
            str = getEndTime(time);
        }else if(StringUtils.equals(ConstantTime.MONTH, type)){
            str = getEndTimeByDay(time);
        }else if(StringUtils.equals(ConstantTime.WEEK, type)){
            str = getEndTimeByDay(time);
        }else{
            LOGGER.error("getStartTimeByType--->所传参数和所列不匹配");
        }
        return str;
    }

    /**
     * 将字符串（年-月，年-月-日，年-月-日 时:分:秒）转化为LocalDate
     */
    public static LocalDate getLocalDate(String time){
        LocalDate localDate;
        if(time.length() == 7){
            localDate = LocalDate.parse(time + "-01", ConstantUtil.DATE_FORMATTER);
        }else if (time.length() == 10){
            localDate = LocalDate.parse(time, ConstantUtil.DATE_FORMATTER);
        }else{
            localDate = LocalDateTime.parse(time, ConstantUtil.DATE_TIME_FORMATTER).toLocalDate();
        }
        return localDate;
    }

    /**
     * 判断当前日期（2021-09-03）是否在 两个时间中
     * @param type 日期类型
     * @param currentTime 数组中的时间
     * @param startTime 批次中的开始时间
     * @param endTime 批次中的结束时间
     * @return JSONObject
     */
    public static JSONObject isDateBetweenTime(String type, String currentTime, String startTime, String endTime){
        JSONObject jsonObject = new JSONObject();
        boolean flog = false;
        LocalDate current = getLocalDate(currentTime);
        LocalDate start = getLocalDate(startTime);
        LocalDate end = getLocalDate(endTime);

        if(current.isEqual(start) && !current.isEqual(end)){
            flog = true;
            jsonObject.put("queryStartTime", startTime);
            jsonObject.put("queryEndTime", getEndTimeByType(type, currentTime));
        }

        if(!current.isEqual(start) && current.isEqual(end)){
            flog = true;
            jsonObject.put("queryStartTime", getStartTimeByType(type, currentTime));
            jsonObject.put("queryEndTime", endTime);
        }

        if(current.isEqual(start) && current.isEqual(end)){
            flog = true;
            jsonObject.put("queryStartTime", startTime);
            jsonObject.put("queryEndTime", endTime);
        }

        if(current.isAfter(start) && current.isBefore(end)){
            flog = true;
            jsonObject.put("queryStartTime", getStartTimeByType(type, currentTime));
            jsonObject.put("queryEndTime", getEndTimeByType(type, currentTime));
        }
        jsonObject.put("flog", flog);
        return jsonObject;
    }

    /**
     * 判断当前年-月（2021-09）是否在 两个时间中
     * @param type 日期类型
     * @param currentTime 数组中的时间
     * @param startTime 批次中的开始时间
     * @param endTime 批次中的结束时间
     * @return JSONObject
     */
    public static JSONObject isYearMonthBetweenTime(String type, String currentTime, String startTime, String endTime){
        JSONObject jsonObject = new JSONObject();
        boolean flog = false;

        if(startTime.startsWith(currentTime) && !endTime.startsWith(currentTime)){
            flog = true;
            jsonObject.put("queryStartTime", startTime);
            jsonObject.put("queryEndTime", getEndTimeByType(type, currentTime));
        }

        if(!startTime.startsWith(currentTime) && endTime.startsWith(currentTime)){
            flog = true;
            jsonObject.put("queryStartTime", getStartTimeByType(type, currentTime));
            jsonObject.put("queryEndTime", endTime);
        }

        if(startTime.startsWith(currentTime) && endTime.startsWith(currentTime)){
            flog = true;
            jsonObject.put("queryStartTime", startTime);
            jsonObject.put("queryEndTime", endTime);
        }

        jsonObject.put("flog", flog);
        return jsonObject;
    }

    /** 往前推几天 */
    public static String getPreWeekStart(String time, String pattern, int num)  {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = (Date) formatter.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        instance.set(Calendar.DATE, instance.get(Calendar.DATE) - num);
        Date instanceTime = instance.getTime();
        return formatter.format(instanceTime);
    }

    /**
     * 将Date转换为LocalDateTime
     */
    public static LocalDateTime dateCovertLocalDateTime(Date date){
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zoneId);
    }

    /**
     * 指定时间范围的时间间隔的时间集合
     * @param sTime 开始时间
     * @param eTime 结束时间
     * @param interval 时间间隔
     * @return 时间集合
     */
    public static List<String> getDuringTimeByInterval(String sTime, String eTime, String interval) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<String> timeList;
        try {
            timeList = new ArrayList<>();
            long startTime = sdf.parse(sTime).getTime();
            long endTime = sdf.parse(eTime).getTime();
            int interValue = Integer.parseInt(interval) * 60 * 1000;
            while (startTime <= endTime) {
                String format = sdf.format(startTime);
                timeList.add(format);
                startTime += interValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
            timeList = new ArrayList<>();
        }
        return timeList;
    }

    /**
     * 指定时间范围的时间间隔的时间集合
     * @param sTime 开始时间
     * @param eTime 结束时间
     * @param interval 时间间隔
     * @return 时间集合
     */
    public static List<String> getDuringTimeByIntervalByFormat(String sTime, String eTime, String interval,String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

        List<String> timeList;
        try {
            timeList = new ArrayList<>();
            long startTime = sdf.parse(sTime).getTime();
            long endTime = sdf.parse(eTime).getTime();
            int interValue = Integer.parseInt(interval) * 60 * 1000;
            while (startTime <= endTime) {
                String format = sdf.format(startTime);
                timeList.add(format);
                startTime += interValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
            timeList = new ArrayList<>();
        }
        return timeList;
    }

    /**
     * 当前：当前日期的 开始日期 和 结束日期
     * @param date 要求date 格式要为 格式要为 yyyy  或者  yyyy-mm
     * @return ImmutablePair<开始日期, 结束日期>
     */
    public static ImmutablePair<String, String> getCurrentDateStr(String date){
        String startTime = getCurStartDateByStr(date);
        LocalDate now = LocalDate.now();
        int year = Integer.parseInt(date.substring(0, 4));
        String endTime = null;
        if(date.length()== ConstantNum.NUMBER_FOUR){
            endTime = now.getYear() == year ? ConstantUtil.DATE_FORMATTER.format(now) : year+"-12-31";
        }else if (date.length()==ConstantNum.NUMBER_SEVEN){
            LocalDate end = LocalDate.parse(date+"-01", ConstantUtil.DATE_FORMATTER);
            endTime = ConstantUtil.DATE_FORMATTER.format((now.getYear()==end.getYear()&&now.getMonthValue()==end.getMonthValue())
                    ? now : end.with(TemporalAdjusters.lastDayOfMonth()));
        }
        return ImmutablePair.of(startTime, endTime);
    }

    public static String getCurStartDateByStr(String date){
        if(date.length()==ConstantNum.NUMBER_FOUR){
            return date+"-01-01";
        }else if(date.length()==ConstantNum.NUMBER_SEVEN){
            return date+"-01";
        }else {
            return date;
        }
    }

    /**
     * 注意：没有处理 年 和 月 类型的时间
     * @param startTime
     * @param endTime
     * @param ts
     * @param tsUnit Y:年;M:月;D:日;H:小时;MIN:分
     * @param isDesc 是否倒叙
     * @return
     */
    public static List<String> getAllTimeInternal(String startTime, String endTime, Integer ts, String tsUnit, boolean isDesc){
        Integer interval;
        switch (tsUnit) {
            case "MIN": interval = ts;break;
            case "H": interval = ts*60;break;
            case "D": interval = ts*60*24;break;
            default:interval=0;
        }
        if(StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)){
            return Collections.emptyList();
        }
        List<String> list = getAllTimeInterval(startTime, endTime, interval);
        LocalDateTime now = LocalDateTime.now();
        if(endTime.substring(0,10).equals(now.toString().substring(0,10))){
            Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()) {
                LocalDateTime time = LocalDateTime.parse(iterator.next(), ConstantUtil.DATE_TIME_FORMATTER);
                if(time.isAfter(now)){
                    iterator.remove();
                }
            }
        }
        if(isDesc){
            list.sort(Comparator.comparing((String s)->s).reversed());
        }
        return list;
    }

    /**
     * 获取从开始时间到结束时间段内按等时间（分钟）分割的时间段列表
     * @param startTime
     * @param endTime
     * @param interval
     * @return
     */
    public static List<String> getAllTimeInterval(String startTime, String endTime, Integer interval) {
        List<String> resList = new ArrayList<>();
        if (ConstantNum.NUMBER_ZERO.equals(interval)) return resList;
        if(StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)){
            return resList;
        }
        try {
            LocalDateTime startDate = LocalDateTime.parse(startTime, ConstantUtil.DATE_TIME_FORMATTER);
            LocalDateTime endData = LocalDateTime.parse(endTime, ConstantUtil.DATE_TIME_FORMATTER);
            boolean endFlag = true;
            int i = 1;
            while (endFlag) {
                LocalDateTime nextTime = startDate.plusMinutes((long) interval * i);
                if (nextTime.compareTo(endData) > 0) {
                    endFlag = false;
                    continue;
                }
                i++;
                resList.add(nextTime.format(ConstantUtil.DATE_TIME_FORMATTER));
            }
            // 加上第一条
            resList.add(0, startDate.format(ConstantUtil.DATE_TIME_FORMATTER));
        } catch (Exception e) {
            LOGGER.error("获取从开始时间到结束时间段内按等时间（分钟）分割的时间段列表出错！！", e);
        }
        return resList;
    }

    public static String getbeforeDayTime() {
        SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -1);
        return format.format(calendar.getTime());
    }

    public static List<ImmutablePair<String, String>> getDayAllTimeInterval(String startTime, String endTime) {
        List<ImmutablePair<String, String>> resList = new ArrayList<>();
        try {
            LocalDate startDate = LocalDate.parse(startTime, ConstantUtil.DATE_FORMATTER);
            LocalDate endData = LocalDate.parse(endTime, ConstantUtil.DATE_FORMATTER);
            boolean endFlag = true;
            int i = 1;
            while (endFlag) {
                LocalDate nextTime = startDate.plusDays(i);
                if (nextTime.compareTo(endData) > 0) {
                    endFlag = false;
                    continue;
                }
                i++;
                String day = nextTime.format(ConstantUtil.DATE_FORMATTER);
                resList.add(ImmutablePair.of(TimeUtil.getStartOfDay(day), TimeUtil.getEndOfDay(day)));
            }
            // 加上第一条
            String start = startDate.format(ConstantUtil.DATE_FORMATTER);
            resList.add(0, ImmutablePair.of(TimeUtil.getStartOfDay(start), TimeUtil.getEndOfDay(start)));
        } catch (Exception e) {
            LOGGER.error("获取开始时间和结束时间", e);
        }
        return resList;
    }

    public static String getDayStrByTimeStr(String time){
        return StringUtils.isBlank(time) ? time : time.substring(0, 10);
    }

    public static List<String> getTimeInterval(String startTime, String endTime) {
        LocalDate endDate = LocalDate.parse(endTime, ConstantUtil.DATE_FORMATTER);
        LocalDate startDate = LocalDate.parse(startTime, ConstantUtil.DATE_FORMATTER);
        List<String> timeList = new ArrayList<>();
        LocalDate localDate = startDate;
        int days = 1;
        while (true) {
            if (endDate.compareTo(localDate) < 0) {
                break;
            } else if (endDate.compareTo(localDate) == 0) {
                timeList.add(localDate.format(ConstantUtil.DATE_FORMATTER));
                break;
            } else {
                timeList.add(localDate.format(ConstantUtil.DATE_FORMATTER));
                localDate = startDate.plusDays(days);
                days++;
            }
        }
        return timeList;
    }

    /**
     * 计算未来最近的整点时间
     * @param executionTime
     * @return
     */
    public static Date calculateHourlyTime(Date executionTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(executionTime);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 根据当前时间计算最近的15分钟时间
     * @param executionTime
     * @return
     */
    public static Date calculateQuarterTime(Date executionTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(executionTime);
        int minute = calendar.get(Calendar.MINUTE);
        if (minute < 15) {
            calendar.set(Calendar.MINUTE, 15);
        } else if (minute < 30) {
            calendar.set(Calendar.MINUTE, 30);
        } else if (minute < 45) {
            calendar.set(Calendar.MINUTE, 45);
        } else {
            calendar.set(Calendar.MINUTE, 0);
            calendar.add(Calendar.HOUR_OF_DAY, 1);
        }
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 根据当前时间计算最近的半小时时间
     * @param executionTime
     * @return
     */
    public static Date calculateHalfTime(Date executionTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(executionTime);
        int minute = calendar.get(Calendar.MINUTE);
        if (minute < 30) {
            calendar.set(Calendar.MINUTE, 30);
        } else {
            calendar.set(Calendar.MINUTE, 0);
            calendar.add(Calendar.HOUR_OF_DAY, 1);
        }
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 时间坐标
     * @param startTime
     * @param endTime
     * @param ts
     * @param tsUnit
     * @return
     */
    public static List<String> getXAxisData(String startTime, String endTime, Integer ts, String tsUnit){
        List<String> times = splitTimeByUnit(startTime, endTime, ts, tsUnit);
        return times.stream().map(t ->convertTimeByUnitOfTime(t, tsUnit)).collect(Collectors.toList());
    }

    /**
     * 根据时间单位转换切割时间
     * @param time
     * @param tsUnit
     * @return
     */
    public static String convertTimeByUnitOfTime(String time, String tsUnit){
        switch (tsUnit) {
            case ConstantDataApi.TS_Y:
                return time.substring(ConstantNum.NUMBER_ZERO, ConstantNum.NUMBER_FOUR);
            case ConstantDataApi.TS_M:
                return time.substring(ConstantNum.NUMBER_FIVE, ConstantNum.NUMBER_SEVEN);
            case ConstantDataApi.TS_D:
                return time.substring(ConstantNum.NUMBER_EIGHT, ConstantNum.NUMBER_TEN);
            case ConstantDataApi.TS_H:
                return time.substring(ConstantNum.NUMBER_ELEVEN, ConstantNum.NUMBER_THIRTEEN);
            case ConstantDataApi.TS_MIN:
                return time.substring(ConstantNum.NUMBER_ELEVEN, ConstantNum.NUMBER_SIXTEEN);
            default:
                throw new RuntimeException("参数类型异常");
        }
    }

    /**
     * 根据时间单位转换时间(切割当前单位之后的时间)
     * @param time
     * @param tsUnit
     * @return
     */
    public static String convertTimeByUnitAfterTime(String time, String tsUnit){
        switch (tsUnit) {
            case ConstantDataApi.TS_Y:
                return time.substring(ConstantNum.NUMBER_ZERO, ConstantNum.NUMBER_FOUR);
            case ConstantDataApi.TS_M:
                return time.substring(ConstantNum.NUMBER_ZERO, ConstantNum.NUMBER_SEVEN);
            case ConstantDataApi.TS_D:
                return time.substring(ConstantNum.NUMBER_ZERO, ConstantNum.NUMBER_TEN);
            case ConstantDataApi.TS_H:
                return time.substring(ConstantNum.NUMBER_ZERO, ConstantNum.NUMBER_THIRTEEN);
            case ConstantDataApi.TS_MIN:
                return time.substring(ConstantNum.NUMBER_ZERO, ConstantNum.NUMBER_SIXTEEN);
            default:
                throw new RuntimeException("参数类型异常");
        }
    }

    /**
     * 根据时间区间计算拆分时间段(全时间格式)
     * @param startTime
     * @param endTime
     * @param ts
     * @param tsUnit
     * @return
     */
    public static List<String> splitTimeByUnit(String startTime, String endTime, Integer ts, String tsUnit){
        ChronoUnit unit;
        switch (tsUnit) {
            case ConstantDataApi.TS_Y: unit = ChronoUnit.YEARS;
                startTime=startTime.substring(ConstantNum.ZERO_INT, ConstantNum.NUMBER_FOUR)+"-01-01 00:00:00";break;
            case ConstantDataApi.TS_M: unit = ChronoUnit.MONTHS;
                startTime=startTime.substring(ConstantNum.ZERO_INT, ConstantNum.NUMBER_SEVEN)+"-01 00:00:00";break;
            case ConstantDataApi.TS_D: unit = ChronoUnit.DAYS;
                startTime=startTime.substring(ConstantNum.ZERO_INT, ConstantNum.NUMBER_ELEVEN)+"00:00:00";break;
            case ConstantDataApi.TS_H: unit = ChronoUnit.HOURS;
                startTime=startTime.substring(ConstantNum.ZERO_INT, ConstantNum.NUMBER_THIRTEEN)+":00:00";break;
            case ConstantDataApi.TS_MIN: unit = ChronoUnit.MINUTES;
                startTime=startTime.substring(ConstantNum.ZERO_INT,ConstantNum.NUMBER_SIXTEEN)+":00";break;
            default: throw new RuntimeException("参数类型异常");
        }

        List<String> list = new ArrayList<>();
        LocalDateTime s = LocalDateTime.parse(startTime, ConstantUtil.DATE_TIME_FORMATTER);
        LocalDateTime e = LocalDateTime.parse(endTime,ConstantUtil.DATE_TIME_FORMATTER);
        while (s.isBefore(e)) {
            list.add(s.format(ConstantUtil.DATE_TIME_FORMATTER));
            s = TimeUtil.offset(s,ts, unit);
        }
        return list;
    }

    /**
     * 时间相减
     */
    public static String timeSut(String time, Integer number, TemporalUnit field){
        String pattern = "(\\d{4}-\\d{2}-\\d{2})\\s([0-2][0-3]:[0-5][0-9]:[0-5][0-9])";
        if(!Pattern.compile(pattern).matcher(time).matches()){
            time = time+"-01 00:00:00";
        }
        LocalDateTime s = LocalDateTime.parse(time, ConstantUtil.DATE_TIME_FORMATTER);
        LocalDateTime result = offset(s, number, field);
        return result.format(ConstantUtil.DATE_TIME_FORMATTER);
    }

    /**
     * 时间偏移
     * @param time
     * @param number
     * @param field
     * @return
     * @param <T>
     */
    public static <T extends Temporal> T offset(T time, long number, TemporalUnit field) {
        if (null == time) {
            return null;
        }
        return (T) time.plus(number, field);
    }

    /**
     * 注意：没有处理 年 和 月 类型的时间
     * @param startTime
     * @param endTime
     * @param ts
     * @param tsUnit Y:年;M:月;D:日;H:小时;MIN:分
     * @param isDesc 是否倒叙
     * @return
     */
    public static List<String> getTimeInternalForAll(String startTime, String endTime, Integer ts, String tsUnit, boolean isDesc){
        Integer interval;
        switch (tsUnit) {
            case "MIN": interval = ts;break;
            case "H": interval = ts*60;break;
            case "D": interval = ts*60*24;break;
            default:interval=0;
        }
        if(StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)){
            return Collections.emptyList();
        }
        List<String> list = getAllTimeInterval(startTime, endTime, interval);
        if(isDesc){
            list.sort(Comparator.comparing((String s)->s).reversed());
        }
        return list;
    }

    /**
     * 使用指定格式，格式化时间
     *
     * @param localDateTime 时间
     * @param dateTimeFormatter 时间格式
     * @return 格式化时间字符串
     */
    public static String getLocalDateTimeStr(LocalDateTime localDateTime, DateTimeFormatter dateTimeFormatter) {
        if(Objects.nonNull(dateTimeFormatter)) {
            return localDateTime.format(dateTimeFormatter);
        }
        return localDateTime.format(df);
    }

    /**
     * 将时间范围切分成指定间隔的时间段
     * @param startDateTime 开始日期
     * @param endDateTime 结束日期
     * @param ts 时间间隔
     * @param tsUnit 时间单位
     * @param formatVal 时间格式化
     * @param handleEndTime 是否处理endTime (endTime减1秒)
     * @return
     */
    public static Map<String, String> splitDateTimeInRange(LocalDateTime startDateTime, LocalDateTime endDateTime, Integer ts, String tsUnit, String formatVal, Boolean handleEndTime) {
        Map<String, String> timeRangeMap = new LinkedHashMap<>();
        // 结束时间统一减1s 左闭右开 避免重叠
        if (handleEndTime) {
            endDateTime = endDateTime.plusSeconds(-1);
        }

        LocalDateTime currentTime = startDateTime;
        // 循环直到当前时间超过结束时间
        while (!currentTime.isAfter(endDateTime)) {
            LocalDateTime tempTime = null;
            if ("MIN".equals(tsUnit)) {
                tempTime = currentTime.plusMinutes(ts);// 增加间隔时间
            } else if ("H".equals(tsUnit)) {
                tempTime = currentTime.plusHours(ts);// 增加间隔时间
            } else if ("D".equals(tsUnit)) {
                tempTime = currentTime.plusDays(ts);// 增加间隔时间
            } else if ("Y".equals(tsUnit)) {
                tempTime = currentTime.plusYears(ts);// 增加间隔时间
            } else {
                throw new ActiveException("时间单位错误");
            }
            // 将当前时间添加到列表
            timeRangeMap.put(currentTime.format(DateTimeFormatter.ofPattern(formatVal)), tempTime.plusSeconds(-1).format(DateTimeFormatter.ofPattern(formatVal)));
            // 如果 下一轮时间 大于 结束时间 跳出循环
            if (tempTime.isAfter(endDateTime)) {
                // 将当前时间添加到列表
                timeRangeMap.put(currentTime.format(DateTimeFormatter.ofPattern(formatVal)), endDateTime.format(DateTimeFormatter.ofPattern(formatVal)));
                break;
            }
            currentTime = tempTime;
        }

        return timeRangeMap;
    }

    public static Map<String, String> splitDateTimeInRange(String startTime, String endTime, Integer ts, String tsUnit, String formatVal, Boolean handleEndTime) {
        LocalDateTime startDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern(formatVal));
        LocalDateTime endDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern(formatVal));
        return splitDateTimeInRange(startDateTime, endDateTime, ts, tsUnit, formatVal, handleEndTime);
    }

}