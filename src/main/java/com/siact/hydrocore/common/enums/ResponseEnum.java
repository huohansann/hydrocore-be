package com.siact.hydrocore.common.enums;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-17 10:15
 * @className : ResponseCode
 * @description : 请求响应结果枚举项
 */
public enum ResponseEnum {
    SUCCESS(200, "request.success", "操作成功"),
    ERROR(500, "request.failed", "操作失败"),
    REQUEST_BAD(400, "request.bad", "请求的数据格式不符!"),
    UNAUTHORIZED(401, "unauthorized", "暂未登录或 token 已经过期"),
    FORBIDDEN(403, "forbidden", "没有相关权限");

    private final Integer code;
    private final String key;
    private String content;

    ResponseEnum(Integer code, String key) {
        this.code = code;
        this.key = key;
    }

    ResponseEnum(Integer code, String key, String content) {
        this.code = code;
        this.key = key;
        this.content = content;
    }

    public Integer code() {
        return code;
    }

    public String key() {
        return key;
    }

    public String content() {
        return content;
    }

    public static ResponseEnum fromKey(String key) {
        for (ResponseEnum value : ResponseEnum.values()) {
            if (value.key().equals(key)) return value;
        }
        return null;
    }
}
