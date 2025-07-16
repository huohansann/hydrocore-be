package com.siact.module.process.enums;

import lombok.Getter;

/**
 * 除泡系统枚举
 */
@Getter
public enum DefoamSystemEnum {
    YES("Y", "有"),
    NO("X", "无"),
    ;
    private final String code;
    private final String desc;

    DefoamSystemEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DefoamSystemEnum getByCode(String code) {
        for (DefoamSystemEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
} 