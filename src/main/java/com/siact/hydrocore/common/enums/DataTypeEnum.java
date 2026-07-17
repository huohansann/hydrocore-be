package com.siact.hydrocore.common.enums;

import lombok.Getter;

@Getter
public enum DataTypeEnum {

    AVG("AVG", "均值"),
    MAX("MAX", "最大值"),
    MIN("MIN", "最小值"),
    LAST("LAST", "最新值"),
    FIRST("FIRST", "最早值"),
    SUM("SUM", "累加"),
    INC("INC", "增量"),
    COUNT("COUNT", "数量");

    private final String type;
    private final String desc;

    DataTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
