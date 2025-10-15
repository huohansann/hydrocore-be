package com.siact.module.process.enums;

import lombok.Getter;

@Getter
public enum FireCycleEnum {
    TWENTY("20", "20MIN"),
    TWENTY_FOUR("24", "24MIN"),
    TWENTY_SEVEN("27", "27MIN"),
    TWENTY_ONE("21", "21MIN");

    private final String code;
    private final String desc;

    FireCycleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FireCycleEnum getByCode(String code) {
        for (FireCycleEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
