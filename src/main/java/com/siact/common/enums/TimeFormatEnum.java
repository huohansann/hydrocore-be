package com.siact.common.enums;

import java.util.regex.Pattern;

public enum TimeFormatEnum {
    YEAR("^\\d{4}$"),
    YEAR_MONTH("^\\d{4}-\\d{2}$"),
    DATE("^\\d{4}-\\d{2}-\\d{2}$"),
    DATE_TIME_MINUTE("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$"),
    DATE_TIME_SECOND("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$"),
    DATE_TIME_HOUR("^\\d{4}-\\d{2}-\\d{2} \\d{2}$"),
    UNKNOWN("");

    private final Pattern pattern;

    TimeFormatEnum(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public static TimeFormatEnum judgeFormat(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return UNKNOWN;

        for (TimeFormatEnum format : values()) {
            if (format != UNKNOWN && format.pattern.matcher(timeStr).matches()) {
                return format;
            }
        }
        return UNKNOWN;
    }
}

