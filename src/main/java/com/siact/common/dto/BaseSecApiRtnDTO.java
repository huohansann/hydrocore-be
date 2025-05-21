package com.siact.common.dto;

import lombok.Data;

@Data
public class BaseSecApiRtnDTO<T> {
    private String code;
    private String msg;
    private T data;
}
