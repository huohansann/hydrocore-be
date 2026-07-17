package com.siact.hydrocore.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 响应信息主体
 *
 * @author siact
 */
@Data
@NoArgsConstructor
public class R<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 成功 */
    public static final int SUCCESS = 200;
    
    /** 失败 */
    public static final int FAIL = 500;

    /** 消息状态码 */
    private int code;

    /** 消息内容 */
    private String msg;

    /** 数据对象 */
    private T data;

    /**
     * 初始化一个新创建的 R 对象
     *
     * @param code 状态码
     * @param msg 返回内容
     */
    public R(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 初始化一个新创建的 R 对象
     *
     * @param code 状态码
     * @param msg 返回内容
     * @param data 数据对象
     */
    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static <T> R<T> success() {
        return new R<>(SUCCESS, "操作成功");
    }

    /**
     * 返回成功数据
     *
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> R<T> success(T data) {
        return new R<>(SUCCESS, "操作成功", data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @return 成功消息
     */
    public static <T> R<T> success(String msg) {
        return new R<>(SUCCESS, msg);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> R<T> success(String msg, T data) {
        return new R<>(SUCCESS, msg, data);
    }

    /**
     * 返回失败消息
     *
     * @return 失败消息
     */
    public static <T> R<T> fail() {
        return new R<>(FAIL, "操作失败");
    }

    /**
     * 返回失败消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 失败消息
     */
    public static <T> R<T> fail(String msg, T data) {
        return new R<>(FAIL, msg, data);
    }

    /**
     * 返回失败消息
     *
     * @param msg 返回内容
     * @return 失败消息
     */
    public static <T> R<T> fail(String msg) {
        return new R<>(FAIL, msg);
    }

    /**
     * 返回失败消息
     *
     * @param code 状态码
     * @param msg 返回内容
     * @return 失败消息
     */
    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg);
    }

    /**
     * 返回数据
     *
     * @param data 数据对象
     * @return 数据
     */
    public static <T> R<T> data(T data) {
        return new R<>(SUCCESS, "操作成功", data);
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> Boolean isError(R<T> ret) {
        return !isSuccess(ret);
    }

    public static <T> Boolean isSuccess(R<T> ret) {
        return 200 == ret.getCode();
    }
}

