package com.siact.hydrocore.common.enums;

import lombok.Getter;

@Getter
public enum ShortCodeEnum {
    LJS("LJS","累计流量"),
    EPF("EPF","正向有功电能");


    private String shortCode;
    private String desc;

    ShortCodeEnum(String shortCode, String desc) {
        this.shortCode = shortCode;
        this.desc = desc;
    }

}
