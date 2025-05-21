package com.siact.module.permission.enums;

/**
 * 用户状态枚举
 *
 * @author wr
 */
public enum UserStatusEnum {
    NORMAL(1, "正常"),
    LOCKED(2, "禁用");

    private final Integer code;
    private final String name;

    UserStatusEnum(Integer code, String message) {
        this.code = code;
        this.name = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 通过name获取枚举code
     */
    public static Integer getCodeByName(String name) {
        for (UserStatusEnum statusEnum : UserStatusEnum.values()) {
            if (statusEnum.getName().equals(name)) {
                return statusEnum.getCode();
            }
        }
        return null;
    }


    /**
     * 通过code获取枚举name
     */
    public static String getNameByCode(Integer code) {
        for (UserStatusEnum statusEnum : UserStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum.getName();
            }
        }
        return null;
    }

}
