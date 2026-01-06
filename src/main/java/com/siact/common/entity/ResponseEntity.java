package com.siact.common.entity;

import lombok.*;

import java.io.Serializable;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-17 9:42
 * @className : ResponseEntity
 * @description : 系统接口统一返回实体
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ResponseEntity<T> implements Serializable {
    private final static long serialVersionUID = 1L;
    private Integer code;
    private String message;
    private T data;
}
