package com.siact.hydrocore.common.exception;

import lombok.Getter;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-08 11:54
 * @className : StompAuthException
 * @description : WebSocket 鉴权异常
 */
public class StompAuthException extends RuntimeException {
    private final @Getter int code;

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public StompAuthException(int code, String message) {
        super(message);
        this.code = code;
    }

}
