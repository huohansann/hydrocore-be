package com.siact.common.enums;

public enum StatusEnum {
    VALID(1, "有效"),
    INVALID(0, "无效");

    private Integer code;
    private String message;

    private StatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static StatusEnum getByCode(Integer code) {
        for (StatusEnum statusEnum : StatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}
