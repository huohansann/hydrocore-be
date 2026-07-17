package com.siact.hydrocore.common.exception;

public class CommonUtilException extends RuntimeException {
    public CommonUtilException(Throwable cause) {
        super(cause);
    }

    public CommonUtilException(String message) {
        super(message);
    }

    public CommonUtilException(String message, Throwable cause) {
        super(message, cause);
    }
}
