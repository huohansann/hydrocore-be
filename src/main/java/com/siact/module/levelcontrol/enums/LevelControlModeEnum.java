package com.siact.module.levelcontrol.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LevelControlModeEnum {
    AI("ai", "AI智控"),
    PID("pid", "PID控制"),
    MANUAL("manual", "人工控制");

    private final String code;
    private final String name;

    public static LevelControlModeEnum fromCode(String code) {
        for (LevelControlModeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知的控制模式: " + code);
    }
}
