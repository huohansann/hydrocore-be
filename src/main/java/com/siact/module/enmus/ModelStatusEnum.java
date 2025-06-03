package com.siact.module.enmus;

public enum ModelStatusEnum {

    RUNNING(1),
    SUCCESS(2),
    FAIL(3);

    private int value;

    ModelStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ModelStatusEnum getEnum(int value) {
        for (ModelStatusEnum statusEnum : ModelStatusEnum.values()) {
            if (statusEnum.getValue() == value) {
                return statusEnum;
            }
        }
        return null;
    }

    public static boolean isSuccess(int value) {
        return SUCCESS.getValue() == value;
    }

    public static boolean isFail(int value) {
        return FAIL.getValue() == value;
    }

}
