package com.siact.module.process.enums;

/**
 * 除泡系统枚举
 */
public enum DefoamingSystemEnum {
    YES(1, "有"),
    NO(2, "无");
    private final int value;
    private final String desc;
    DefoamingSystemEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    public int getValue() { return value; }
    public String getDesc() { return desc; }
    public static DefoamingSystemEnum fromValue(int value) {
        for (DefoamingSystemEnum e : values()) {
            if (e.value == value) return e;
        }
        return null;
    }
} 