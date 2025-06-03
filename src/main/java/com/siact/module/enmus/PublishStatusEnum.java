package com.siact.module.enmus;

public enum PublishStatusEnum {

    UNPUBLISHED(1, "未发布"),
    PUBLISHING(2, "发布中"),
    PUBLISHED(3, "已发布");

    private int code;
    private String message;

    private PublishStatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static PublishStatusEnum getEnum(int code) {
        for (PublishStatusEnum publishStatusEnum : PublishStatusEnum.values()) {
            if (publishStatusEnum.getCode() == code) {
                return publishStatusEnum;
            }
        }
        return null;
    }

    public static String getMessage(int code) {
        for (PublishStatusEnum publishStatusEnum : PublishStatusEnum.values()) {
            if (publishStatusEnum.getCode() == code) {
                return publishStatusEnum.getMessage();
            }
        }
        return null;
    }

    public static int getCode(String message) {
        for (PublishStatusEnum publishStatusEnum : PublishStatusEnum.values()) {
            if (publishStatusEnum.getMessage().equals(message)) {
                return publishStatusEnum.getCode();
            }
        }
        return -1;
    }

    public static boolean contains(int code) {
        for (PublishStatusEnum publishStatusEnum : PublishStatusEnum.values()) {
            if (publishStatusEnum.getCode() == code) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(String message) {
        for (PublishStatusEnum publishStatusEnum : PublishStatusEnum.values()) {
            if (publishStatusEnum.getMessage().equals(message)) {
                return true;
            }
        }
        return false;
    }
}
