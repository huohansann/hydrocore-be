package com.siact.core.web.advice;

import com.siact.common.annotation.NoResponseAdvice;
import com.siact.common.annotation.SuccessMessage;
import com.siact.common.entity.ResponseEntity;
import com.siact.common.enums.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-17 10:01
 * @className : ResponseBodyAdvice
 * @description : 响应实体统一处理
 */
@Slf4j
@RestControllerAdvice
public class ResponseBodyAdvice implements org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        if (returnType.getDeclaringClass().isAnnotationPresent(NoResponseAdvice.class)) return false;
        return !Objects.requireNonNull(returnType.getMethod()).isAnnotationPresent(NoResponseAdvice.class);
    }

    /**
     * 由于项目原有代码响应结构类型混乱, 再次进行统一拦截配置, 并对响应结果进行统一
     */
    @Override
    public @SuppressWarnings("unchecked") Object beforeBodyWrite(@Nullable Object body, @NotNull MethodParameter returnType, @NotNull MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // 兼容原本的响应类型
        if (body instanceof com.siact.common.R) {
            com.siact.common.R<Object> result = ((com.siact.common.R<Object>) body);
            return ResponseEntity.builder().code(result.getCode()).message(result.getMsg()).data(result.getData()).build();
        }
        if (body instanceof com.siact.common.result.R) {
            com.siact.common.result.R<Object> result = ((com.siact.common.result.R<Object>) body);
            return ResponseEntity.builder().code(result.getCode()).message(result.getMessage()).data(result.getData()).build();
        }

        if (body instanceof ResponseEntity<?>) return body;

        String messageKey = ResponseEnum.SUCCESS.getKey();
        if (ObjectUtils.isNotEmpty(returnType)) {
            SuccessMessage sma = returnType.getMethodAnnotation(SuccessMessage.class);
            if (ObjectUtils.isNotEmpty(sma)) messageKey = sma.value();
        }

        ResponseEnum resp = ResponseEnum.fromKey(messageKey);
        if (ObjectUtils.isEmpty(resp)) {
            log.warn("当前 SuccessMessage Key: {} 无对应响应枚举项", messageKey);
            resp = ResponseEnum.SUCCESS;
        }
        return ResponseEntity.builder().code(resp.getCode()).message(resp.getContent()).data(body).build();
    }
}
