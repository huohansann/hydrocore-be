package com.siact.hydrocore.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BizException extends RuntimeException {
    private String code = "600";

    public BizException(String message) {
        this("600", message);
    }

    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }
}
