package com.siact.common.result;

import com.siact.common.constant.HttpStatus;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回对象
 *
 * @param <T> 数据类型
 * @author example
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 成功
     */
    public static final int SUCCESS = ResultCode.SUCCESS.getCode();

    /**
     * 失败
     */
    public static final int FAIL = HttpStatus.FAIL;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 私有构造方法，禁止直接创建
     */
    private R() {
    }

    public R(T data, int code, String msg) {
        this.code = code;
        this.message = msg;
        this.data = data;
    }

    public static <T> R<T> data() {
        return new R(null, SUCCESS, null);
    }

    public static <T> R<T> success(String msg) {
        return new R(null, SUCCESS, msg);
    }

    public static <T> R<T> data(T data) {
        return new R(data, SUCCESS, null);
    }

    public static <T> R<T> data(T data, String msg) {
        return new R(data, SUCCESS, msg);
    }

    /**
     * 失败返回结果
     */
    public static <T> R<T> fail() {
        return fail(ResultCode.FAILED);
    }

    /**
     * 失败返回结果
     *
     * @param message 提示信息
     */
    public static <T> R<T> fail(String message) {
        return result(null, ResultCode.FAILED.getCode(), message, false);
    }

    /**
     * 失败返回结果
     *
     * @param errorCode 错误码
     */
    public static <T> R<T> fail(IErrorCode errorCode) {
        return result(null, errorCode.getCode(), errorCode.getMessage(), false);
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> R<T> validateFailed() {
        return fail(ResultCode.VALIDATE_FAILED);
    }

    /**
     * 参数验证失败返回结果
     *
     * @param message 提示信息
     */
    public static <T> R<T> validateFailed(String message) {
        return result(null, ResultCode.VALIDATE_FAILED.getCode(), message, false);
    }

    /**
     * 未登录返回结果
     */
    public static <T> R<T> unauthorized() {
        return fail(ResultCode.UNAUTHORIZED);
    }

    /**
     * 未授权返回结果
     */
    public static <T> R<T> forbidden() {
        return fail(ResultCode.FORBIDDEN);
    }

    /**
     * 返回结果
     *
     * @param data    数据
     * @param code    状态码
     * @param message 消息
     * @param success 是否成功
     */
    private static <T> R<T> result(T data, Integer code, String message, Boolean success) {
        R<T> result = new R<>();
        result.setCode(code);
        result.setData(data);
        result.setMessage(message);
        result.setSuccess(success);
        return result;
    }
} 