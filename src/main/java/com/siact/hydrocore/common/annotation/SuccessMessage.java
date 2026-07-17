package com.siact.hydrocore.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-17 10:11
 * @className : SuccessMessage
 * @description : 请求响应成功信息注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SuccessMessage {
    String value();
}
