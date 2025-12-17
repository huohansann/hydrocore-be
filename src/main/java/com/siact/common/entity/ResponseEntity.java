package com.siact.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-17 9:42
 * @className : ResponseEntity
 * @description : 系统接口统一返回实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseEntity<T> implements Serializable {
    private final static long serialVersionUID = 1L;
    private Integer code;
    private String message;
    private T data;
}
