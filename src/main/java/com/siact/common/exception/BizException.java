package com.siact.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizException extends RuntimeException{
    private String code = "600";
    private String message;

    public BizException(String message) {
        super(message);
    }
}
