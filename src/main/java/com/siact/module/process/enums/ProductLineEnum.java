package com.siact.module.process.enums;

import lombok.Getter;

/**
 * 产线枚举
 */
@Getter
public enum ProductLineEnum {
    THREE("Ⅲ", "0", "三条产线"),
    FOUR("Ⅳ", "1", "四条产线");
    private final String code;
    private final String binaryCode;
    private final String desc;

    ProductLineEnum(String code, String binaryCode, String desc) {
        this.code = code;
        this.binaryCode = binaryCode;
        this.desc = desc;
    }

    public static ProductLineEnum getByCode(String code) {
        for (ProductLineEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
