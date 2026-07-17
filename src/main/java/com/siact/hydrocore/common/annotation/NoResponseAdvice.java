package com.siact.hydrocore.common.annotation;

import java.lang.annotation.*;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-17 10:04
 * @className : NoResponseAdvice
 * @description : 响应处理屏蔽
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoResponseAdvice {
}
