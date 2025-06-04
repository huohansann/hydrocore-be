package com.siact.module.process.enums;

/**
 * 更换设备枚举
 */
public enum ReplaceMachineEnum {
    NORMAL(1, "正常"),
    REPLACED(2, "换机");
    private final int value;
    private final String desc;
    ReplaceMachineEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    public int getValue() { return value; }
    public String getDesc() { return desc; }
    public static ReplaceMachineEnum fromValue(int value) {
        for (ReplaceMachineEnum e : values()) {
            if (e.value == value) return e;
        }
        return null;
    }
} 