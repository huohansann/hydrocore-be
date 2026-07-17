package com.siact.hydrocore.common.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Boolean success;
    private Integer code;
    private String message;
    private T data;
    private String traceId;

    public static <T> ApiResponse<T> success(T data) {
        return success(data, ApiResponseCode.SUCCESS.getMessage(), "");
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return success(data, message, "");
    }

    public static <T> ApiResponse<T> success(T data, String message, String traceId) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(ApiResponseCode.SUCCESS.getCode())
                .message(message)
                .data(data)
                .traceId(traceId)
                .build();
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return fail(code, message, null, "");
    }

    public static <T> ApiResponse<T> fail(int code, String message, String traceId) {
        return fail(code, message, null, traceId);
    }

    public static <T> ApiResponse<T> fail(int code, String message, T data, String traceId) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .data(data)
                .traceId(traceId)
                .build();
    }
}
