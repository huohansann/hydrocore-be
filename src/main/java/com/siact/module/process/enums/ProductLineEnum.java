package com.siact.module.process.enums;

import lombok.Getter;

/**
 * 产线枚举
 */
@Getter
public enum ProductLineEnum {
    THREE("Ⅲ", "三条产线"),
    FOUR("Ⅳ", "四条产线");
    private final String code;
    private final String desc;

    ProductLineEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProductLineEnum getByCode(String code) {
        for (ProductLineEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
