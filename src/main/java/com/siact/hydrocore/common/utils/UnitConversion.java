package com.siact.hydrocore.common.utils;

import com.siact.hydrocore.common.constant.ConstantSymbol;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tuyj
 * @version 1.0
 * @date 2020/5/25 11:00
 */
public class UnitConversion {

    /**
     * 根据年月 获取对应的月份 天数 (yyyy-MM)
     */
    public static int getDaysByYearMonth(String time) {
        int maxDate = 0;
        try {
            int year = Integer.parseInt(time.substring(0, time.indexOf("-")));
            int month = Integer.parseInt(time.substring(time.lastIndexOf("-") + 1));
            Calendar a = Calendar.getInstance();
            a.set(Calendar.YEAR, year);
            a.set(Calendar.MONTH, month - 1);
            a.set(Calendar.DATE, 1);
            a.roll(Calendar.DATE, -1);
            maxDate = a.get(Calendar.DATE);
        }catch (Exception e){
            return 0;
        }
        return maxDate;
    }
    /**
     * 日期合法校验
     * @param time
     * @return
     */
    public static boolean isDate(String time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        try {
            sdf.setLenient(false);
            sdf.parse(time);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * 日期合法校验
     * @param time
     * @return
     */
    public static boolean isDates(String time, String type){
        SimpleDateFormat sdf = new SimpleDateFormat(type);
        try {
            sdf.setLenient(false);
            sdf.parse(time);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }


    /**
     * 提供精确的加法运算
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    public static double add(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * 提供精确的减法运算
     * @param v1 被减数
     * @param v2 减数
     * @return 两个参数的差
     */
    public static double sub(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 提供精确的乘法运算
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static double mul(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    /**
     * 提供精确的乘法运算
     * @param b1,
     * @param b2
     * @param len 保留小数位
     * @return double
     */
    public static BigDecimal bigDecimalMul(BigDecimal b1, BigDecimal b2, Integer len) {
        return b1.multiply(b2).setScale(len, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 两个Double数相除
     * @param v1
     * @param v2
     * @param len 保留小数位
     * @return Double
     */
    public static double divs(Double v1, Double v2, Integer len){
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.divide(b2,len, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static BigDecimal divsNew(Double v1, Double v2, Integer len){
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.divide(b2,len, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 两个Double数相除
     * @param v1
     * @param v2
     * @return Double
     */
    public static double div(Double v1, Double v2){
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.divide(b2,2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 两个Double数相除(末位置直接舍掉)
     * @param v1
     * @param v2
     * @return Double
     */
    public static double divdown(Double v1, Double v2){
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.divide(b2,2, BigDecimal.ROUND_DOWN).doubleValue();
    }

    /**
     * double原样输出/取消科学计数法
     * @param d
     * @return
     */
    public static String formatDouble(double d) {
        NumberFormat nf = NumberFormat.getInstance();
        //设置保留多少位小数
        nf.setMaximumFractionDigits(20);
        // 取消科学计数法
        nf.setGroupingUsed(false);
        //返回结果
        return nf.format(d);
    }

    /**
     *  double原样输出/取消科学计数法
     * @param d
     * @param len
     * @return
     */
    public static String formatDouble(double d, int len) {
        NumberFormat nf = NumberFormat.getInstance();
        //设置保留多少位小数
        nf.setMaximumFractionDigits(len);
        // 取消科学计数法
        nf.setGroupingUsed(false);
        //返回结果
        return nf.format(d);
    }

    /**
     * 判断一个double是否为0
     * @param b
     * @return boolean
     */
    public static boolean  isItZero(double b){
        BigDecimal data1 = new BigDecimal(0d);
        BigDecimal data2 = new BigDecimal(b);
        int result = data1.compareTo(data2);
        if(0 == result){
            return true;
        }else{
            return false;
        }
    }

    /**
     * double数据精度化，i为精度位数
     * @param d
     * @param i
     * @return
     */
    public static double doublePre(double d, int i) {
        BigDecimal b = new BigDecimal(d);
        return b.setScale(i, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static Object bigDecimalPre(BigDecimal value, int i) {
        if(null == value){
            return ConstantSymbol.SHORT_LINE;
        }
        return value.setScale(i, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal doublePreBigDecimal(String d, int i) {
        BigDecimal b = new BigDecimal(d);
        return b.setScale(i, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * double数据精度化，i为精度位数  解决科学技术法输出
     * @param d
     * @param i
     * @return
     */
    public static String doublePreString(double d, int i) {
        BigDecimal b = new BigDecimal(String.valueOf(d));
        b.setScale(i, BigDecimal.ROUND_HALF_UP);
        return formatDouble(b.doubleValue(), i);
    }

    //map根据key进行排序
    public static  <K extends Comparable<? super K>, V > Map<K, V> sortByKey(Map<K, V> map) {
        Map<K, V> result = new LinkedHashMap<>();

        map.entrySet().stream()
                .sorted(Map.Entry.<K, V>comparingByKey()
                ).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static <T> List<T> castList(Object obj, Class<T> clazz)
    {
        List<T> result = new ArrayList<T>();
        if(obj instanceof List<?>)
        {
            for (Object o : (List<?>) obj)
            {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }

    /**
     * 获取map中最后一个键值对
     * @param map
     * @param <K>
     * @param <V>
     * @return
     */
    public static  <K extends Comparable<? super K>, V > Map<K, V> lastMap(Map<K, V> map){
        Map<K, V> result = null;
        try {
            result = new LinkedHashMap<>();
            Field tail = map.getClass().getDeclaredField("tail");
            tail.setAccessible(true);
            Map.Entry<K,V> entry= (Map.Entry<K, V>) tail.get(map);
            result.put(entry.getKey(),entry.getValue());
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
           return null;
        }
        return result;
    }

    public static List<String> timeIntervalCount(String start, String end){
        int startInt = Integer.parseInt(start.split(":")[0]);
        int endInt = Integer.parseInt(end.split(":")[0]);
        List<String> timeList = new ArrayList<>();
        if (endInt > startInt){
            for (int i = startInt; i < endInt; i++){
                timeHandle(i, timeList);
            }
        } else {
            //设置跨00
            for (int i = startInt; i < 24; i++){
                timeHandle(i, timeList);
            }

            for (int i = 0; i < endInt; i++){
                timeHandle(i, timeList);
            }
        }
        return timeList;
    }

    public static void timeHandle(int i, List<String> timeList){
        if (i < 10){
            timeList.add("0" + i + ":59:59");
        } else {
            timeList.add(i + ":59:59");
        }
    }

    public static List<String> dateIntervalCount(String start, String end){
        List<String> dateList = new ArrayList<>();
        LocalDate startTime = LocalDate.parse(start, formatter);
        LocalDate endTime = LocalDate.parse(end, formatter);
        if (endTime.isAfter(startTime) || endTime.isEqual(startTime)){
            int day = (int) (endTime.toEpochDay() - startTime.toEpochDay());
            for (int i = 0; i <= day; i++){
                dateList.add(startTime.plusDays(i).toString());
            }
        }
        return dateList;
    }

    public static List<String> dateWithTime(List<String> dateList, List<String> timeList){
        List<String> dateTimeList = new ArrayList<>();
        dateList.forEach(date -> {
            timeList.forEach(time -> {
                dateTimeList.add(date + " " + time);
            });
        });
        return dateTimeList;
    }

    public static List<String> dateWithTimeHandle(List<String> dateList){
        List<String> dateTimeList = new ArrayList<>();
        dateList.forEach(date -> {
            dateTimeList.add(date + " 23:59:59");
        });
        return dateTimeList;
    }

    /**
     * 时间间隔格式处理
     * @param timeInterval
     * @return
     */
    public static String timeIntervalDis(String timeInterval){
        if (timeInterval.contains("d")){
            int value = (Integer.parseInt(timeInterval.replace("d", ""))) * 60 * 24;
            return String.valueOf(value);
        }else if (timeInterval.contains("m")){
            return timeInterval.replace("m", "");
        }else {
            return timeInterval;
        }
    }


    /**
     * 比较两个值，转为上升或下降
     * @param val1
     * @param val2
     * @return
     */
    public static String compareVal(Double val1, Double val2) {

        if (val1 == null || val2 == null) {
            return null;
        }

        if (val1 > val2) {
            return "up";
        }

        if (val1 < val2) {
            return "down";
        }

        return "equal";
    }

}
