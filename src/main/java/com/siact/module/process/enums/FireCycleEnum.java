package com.siact.module.process.enums;

import lombok.Getter;

@Getter
public enum FireCycleEnum {
    TWENTY("20", "1", "20MIN"),
    TWENTY_SEVEN("27", "0", "27MIN");
    private final String code;
    private final String binaryCode;
    private final String desc;

    FireCycleEnum(String code, String binaryCode, String desc) {
        this.code = code;
        this.binaryCode = binaryCode;
        this.desc = desc;
    }

    public static FireCycleEnum getByCode(String code) {
        for (FireCycleEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
