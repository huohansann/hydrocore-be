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

    /**
     * 根据code获取type
     *
     * @param typeCode
     * @return
     */
    public static Integer getTypeByCode(String typeCode) {
        for (PredictedTypeEnum typeEnum : PredictedTypeEnum.values()) {
            if (typeEnum.code.equals(typeCode)) {
                return typeEnum.type;
            }
        }
        return null;
    }
}
