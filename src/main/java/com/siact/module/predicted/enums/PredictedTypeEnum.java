package com.siact.module.predicted.enums;

public enum PredictedTypeEnum {
    SINGLE(1, "SINGLE"),
    MULTI(2, "MULTI"),
    ;

    private final Integer type;
    private final String code;

    PredictedTypeEnum(Integer type, String code) {
        this.type = type;
        this.code = code;
    }
}
