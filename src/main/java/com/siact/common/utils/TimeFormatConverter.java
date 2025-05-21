package com.siact.common.utils;

import com.siact.common.enums.TimeFormatEnum;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

/**
 * @desc: 时间格式转换
 * @author: zhangwentao
 * @create: 2025-04-17 14:38
 */
public class TimeFormatConverter {
    // 目标格式
    private static final String TARGET_FORMAT = "yyyy-MM-dd HH:mm:ss";
    // 源格式
    private static final Map<TimeFormatEnum, String> FORMAT_MAP = new HashMap<>();

    // 获取时间单位
    private static final Map<TimeFormatEnum, String> UNIT_MAPPING = new EnumMap<>(TimeFormatEnum.class);

    // 获取返回时间格式
    private static final Map<TimeFormatEnum, String> FORMAT_MAPPING = new EnumMap<>(TimeFormatEnum.class);


    // 默认的格式
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String END_TIME = " 23:59:59";

    // 默认的格式
    static {
        FORMAT_MAP.put(TimeFormatEnum.YEAR, "yyyy");
        FORMAT_MAP.put(TimeFormatEnum.YEAR_MONTH, "yyyy-MM");
        FORMAT_MAP.put(TimeFormatEnum.DATE, "yyyy-MM-dd");
        FORMAT_MAP.put(TimeFormatEnum.DATE_TIME_MINUTE, "yyyy-MM-dd HH:mm");
        FORMAT_MAP.put(TimeFormatEnum.DATE_TIME_SECOND, TARGET_FORMAT);
    }

    // 默认的时间单位
    static {
        UNIT_MAPPING.put(TimeFormatEnum.YEAR, "M");
        UNIT_MAPPING.put(TimeFormatEnum.YEAR_MONTH, "D");
        UNIT_MAPPING.put(TimeFormatEnum.DATE, "H");
        UNIT_MAPPING.put(TimeFormatEnum.DATE_TIME_HOUR, "MIN");  // 需在枚举中增加DATE_TIME_HOUR类型
        UNIT_MAPPING.put(TimeFormatEnum.DATE_TIME_MINUTE, "S");
        UNIT_MAPPING.put(TimeFormatEnum.DATE_TIME_SECOND, "S"); // 最细粒度无单位
    }

    // 默的FORMAT
    static {
        FORMAT_MAPPING.put(TimeFormatEnum.YEAR, "MM");
        FORMAT_MAPPING.put(TimeFormatEnum.YEAR_MONTH, "dd");
        FORMAT_MAPPING.put(TimeFormatEnum.DATE, "HH");
        FORMAT_MAPPING.put(TimeFormatEnum.DATE_TIME_HOUR, "mm");  // 需在枚举中增加DATE_TIME_HOUR类型
        FORMAT_MAPPING.put(TimeFormatEnum.DATE_TIME_MINUTE, "dd");
        FORMAT_MAPPING.put(TimeFormatEnum.DATE_TIME_SECOND, "dd"); // 最细粒度无单位
    }


    /**
     * 将时间字符串转换为yyyy-MM-dd HH:mm:ss格式
     *
     * @param timeStr 时间字符串
     * @param type    时间类型：0代表开始时间，1代表结束时间
     * @return 转换后的时间字符串，如果转换失败则返回null
     */
    public static String convertToFullFormat(String timeStr, int type) {
        if (timeStr == null) {return null;}

        // 结束时间
        if (type == 1) {
            return getEndTime(timeStr);
        }
        // 开始时间
        else {
            TimeFormatEnum format = TimeFormatEnum.judgeFormat(timeStr);
            if (format == TimeFormatEnum.DATE_TIME_SECOND) {
                return timeStr;
            }
            try {
                SimpleDateFormat sourceFormat = new SimpleDateFormat(FORMAT_MAP.get(format));
                Date date = sourceFormat.parse(timeStr);
                return new SimpleDateFormat(TARGET_FORMAT).format(date);
            } catch (ParseException e) {
                return null; // 或抛出 IllegalArgumentException
            }
        }

    }

    /**
     * 获取时间单位
     *
     * @param timeStr 时间字符串
     * @return 时间单位，如果无法识别则返回"D"
     */
    public static String getTimeUnit(String timeStr) {
        TimeFormatEnum format = TimeFormatEnum.judgeFormat(timeStr);
        return UNIT_MAPPING.getOrDefault(format, "D");
    }

    /**
     * 获取时间格式
     *
     * @param timeStr 时间字符串
     * @return 时间格式，如果无法识别则返回"D"
     */
    public static String getTimeFormat(String timeStr) {
        TimeFormatEnum format = TimeFormatEnum.judgeFormat(timeStr);
        return FORMAT_MAPPING.getOrDefault(format, "D");
    }

    /**
     * 按照单位生成默认时间范围
     *
     * @param unit 时间单位
     * @return 时间范围字符串
     */
    public static String generateRangeByUnit(String unit) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime start;
        switch (unit) {
            case "Y":
                start = now.withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0); // 年初
                break;
            case "M":
                start = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0); // 年初
                break;
            case "D":
                start = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0); // 月初
                break;
            case "H":
                start = now.withHour(0).withMinute(0).withSecond(0); // 日初
                break;
            case "MIN":
                start = now.withMinute(0).withSecond(0); // 小时初
                break;
            default:
                start = now.withHour(0).withMinute(0).withSecond(0); // 日初
                break;
        }
        return formatRange(start, now);
    }


    /**
     * 生成指定时间单位的默认时间范围
     * @param time 时间字符串（支持格式：yyyy, yyyy-MM, yyyy-MM-dd, yyyy-MM-dd HH, yyyy-MM-dd HH:mm）
     * @return 时间范围字符串（格式：yyyy-MM-dd HH:mm:ss至yyyy-MM-dd HH:mm:ss）
     * @throws IllegalArgumentException 当输入格式不符合要求时抛出
     */
    public static String generateRangeTime(String time) {

        // 统一获取当前时间对象
        final LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now;
        LocalDateTime start;

        try {
            final TimeFormatEnum format = TimeFormatEnum.judgeFormat(time);

            switch (format) {
                case YEAR:
                    // 格式校验
                    validateYearFormat(time);
                    int year = Integer.parseInt(time);

                    // 非当年处理
                    if (year != now.getYear()) {
                        return formatRange(
                                LocalDateTime.of(year, 1, 1, 0, 0, 0),
                                LocalDateTime.of(year, 12, 31, 23, 59, 59)
                        );
                    }
                    // 当年处理
                    start = now.withMonth(1)
                            .withDayOfMonth(1)
                            .withHour(0)
                            .withMinute(0)
                            .withSecond(0);
                    break;
                case YEAR_MONTH:
                    int[] ym = parseYearMonth(time);
                    // 非当月处理
                    if (ym[0] != now.getYear() || ym[1] != now.getMonthValue()) {
                        LocalDateTime temp = LocalDateTime.of(ym[0], ym[1], 1, 0, 0, 0);
                        LocalDateTime monthEnd = temp.withDayOfMonth(temp.toLocalDate().lengthOfMonth())
                                .withHour(23)
                                .withMinute(59)
                                .withSecond(59);
                        return formatRange(temp, monthEnd);
                    }
                    // 当月处理
                    start = now.withDayOfMonth(1)
                            .withHour(0)
                            .withMinute(0)
                            .withSecond(0);
                    break;
                case DATE:
                    // 非当天处理
                    if (!time.equals(now.format(FORMATTER))) {
                        LocalDateTime parsed = LocalDateTime.parse(time + " 00:00:00", FORMATTER);
                        return formatRange(parsed, parsed.plusDays(1).minusSeconds(1));
                    }
                    // 当天处理
                    start = now.withHour(0)
                            .withMinute(0)
                            .withSecond(0);
                    break;
                case DATE_TIME_HOUR:
                    // 非小时处理
                    if (!time.equals(now.format(FORMATTER))) {
                        LocalDateTime parsed = LocalDateTime.parse(time + ":00:00", FORMATTER);
                        return formatRange(parsed, parsed.plusHours(1).minusSeconds(1));
                    }
                    start = now.withMinute(0)
                            .withSecond(0);
                    break;
                case DATE_TIME_MINUTE:
                    // 非分钟处理
                    if (!time.equals(now.format(FORMATTER))) {
                        LocalDateTime parsed = LocalDateTime.parse(time + ":00", FORMATTER);
                        return formatRange(parsed, parsed.plusMinutes(1).minusSeconds(1));
                    }
                    start = now.withSecond(0);
                    break;
                default:
                    return handleDefaultCase(time, now);
            }

            return formatRange(start, end);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format: " + time, e);
        }
    }

    /**
     * 处理默认情况（非法格式的容错处理）
     */
    private static String handleDefaultCase(String time, LocalDateTime now) {
        // 如果时间为空，则默认为当前时间，按照天维度处理
        if (StringUtils.isBlank(time)) {
            time = now.format(FORMATTER).substring(0, 7);
            return generateRangeTime(time);
        }
        // 格式修正尝试
        if (time.length() > 10) {
            try {
                time = LocalDate.parse(time.substring(0, 10)).format(DateTimeFormatter.ISO_DATE);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Invalid date format", ex);
            }
        }

        // 修正后再次尝试解析
        if (!time.equals(now.format(FORMATTER))) {
            try {
                LocalDateTime parsed = LocalDateTime.parse(time + " 00:00:00", FORMATTER);
                return formatRange(parsed, parsed.plusDays(1).minusSeconds(1));
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Malformed time string: " + time);
            }
        }
        return formatRange(now.withHour(0).withMinute(0).withSecond(0), now);
    }

    /**
     * 年份格式校验（必须为4位数字）
     */
    private static void validateYearFormat(String time) {
        if (!time.matches("^\\d{4}$")) {
            throw new IllegalArgumentException("Invalid year format, expected: yyyy");
        }
    }

    /**
     * 解析年月字符串
     */
    private static int[] parseYearMonth(String time) {
        String[] parts = time.split("-");
        if (parts.length != 2 || !parts[0].matches("\\d{4}") || !parts[1].matches("0[1-9]|1[0-2]")) {
            throw new IllegalArgumentException("Invalid month format, expected: yyyy-MM");
        }
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }


    /**
     * 生成时间范围
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 时间范围字符串
     */
    private static String formatRange(LocalDateTime start, LocalDateTime end) {
        return String.format("%s至%s",
                start.format(FORMATTER),
                end.format(FORMATTER)
        );
    }

    /**
     * 获取指定年份和月份的天数
     *
     * @param timeStr 时间字符串
     * @return 天数
     */
    public static String getEndTime(String timeStr) {
        // 获取时间的格式
        TimeFormatEnum format = TimeFormatEnum.judgeFormat(timeStr);
        int year = Integer.parseInt(timeStr.substring(0, 4));
        int month = 0;
        int day = 0;
        switch (format) {
            case YEAR:
                // 默认年份的月份数设为12
                month = 12;
                // 获取对应月份的天数
                day = safeGetMonthDays(year, month).getAsInt();
                timeStr = year + "-" + month + "-" + day + END_TIME;
                break;
            case YEAR_MONTH:
                // 默认年份的月份数设为12
                month = Integer.parseInt(timeStr.substring(5, 7));
                // 获取对应月份的天数
                day = safeGetMonthDays(year, month).getAsInt();
                if (month < 10) {
                    timeStr = year + "-0" + month + "-" + day + END_TIME;
                } else {
                    timeStr = year + "-" + month + "-" + day + END_TIME;
                }
                break;
            case DATE:
                timeStr = timeStr.substring(0, 10) + END_TIME;
                break;
            case DATE_TIME_HOUR:
                timeStr = timeStr.substring(0, 13) + ":59:59";
                break;
            case DATE_TIME_MINUTE:
                timeStr = timeStr.substring(0, 16) + ":59";
                break;
            default:
                break;
        }
        return timeStr;
    }

    public static OptionalInt safeGetMonthDays(int year, int month) {
        try {
            return OptionalInt.of(YearMonth.of(year, month).lengthOfMonth());
        } catch (DateTimeException e) {
            return OptionalInt.empty();
        }
    }
}
