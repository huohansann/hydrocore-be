package com.siact.common.exception;

import com.siact.common.entity.ResponseEntity;
import com.siact.common.enums.ResponseEnum;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public @ExceptionHandler(value = IOException.class) ResponseEntity<String> handleIOException(IOException e, HandlerMethod method) {
        ApiOperation operation = method.getMethodAnnotation(ApiOperation.class);
        log.error("{} error", Objects.requireNonNull(operation).value(), e);
        return ResponseEntity.<String>builder().code(ResponseEnum.ERROR.code()).message("IO 异常").build();
    }

    public @ExceptionHandler(value = MethodArgumentNotValidException.class) ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HandlerMethod method) {
        ApiOperation operation = method.getMethodAnnotation(ApiOperation.class);
        log.error("{} error", Objects.requireNonNull(operation).value(), e);
        BindingResult bindingResult = e.getBindingResult();
        ObjectError allErrors = bindingResult.getAllErrors().get(0);
        return ResponseEntity.<String>builder().code(ResponseEnum.ERROR.code()).message(allErrors.getDefaultMessage()).build();
    }

    public @ExceptionHandler(value = ConstraintViolationException.class) ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e, HandlerMethod method) {
        ApiOperation operation = method.getMethodAnnotation(ApiOperation.class);
        log.error("{} error", Objects.requireNonNull(operation).value(), e);
        return ResponseEntity.<String>builder().code(ResponseEnum.ERROR.code()).message(e.getMessage()).build();
    }

    public @ExceptionHandler(value = ParseException.class) ResponseEntity<String> handleParseException(ParseException e, HandlerMethod method) {
        ApiOperation operation = method.getMethodAnnotation(ApiOperation.class);
        log.error("{} error", Objects.requireNonNull(operation).value(), e);
        return ResponseEntity.<String>builder().code(ResponseEnum.REQUEST_BAD.code()).message(ResponseEnum.REQUEST_BAD.content()).build();
    }

    public @ExceptionHandler(value = BizException.class) ResponseEntity<String> handleBizException(BizException e, HandlerMethod method) {
        ApiOperation operation = method.getMethodAnnotation(ApiOperation.class);
        log.error("发生业务异常, {} error", Objects.requireNonNull(operation).value(), e);
        return ResponseEntity.<String>builder().code(ResponseEnum.ERROR.code()).message(e.getMessage()).build();
    }

    public @ExceptionHandler(value = CustomException.class) ResponseEntity<String> handleCustomException(CustomException e, HandlerMethod method) {
        ApiOperation operation = method.getMethodAnnotation(ApiOperation.class);
        log.error("发生自定义异常, {} error", Objects.requireNonNull(operation).value(), e);
        return ResponseEntity.<String>builder().code(ResponseEnum.ERROR.code()).message(e.getMessage()).build();
    }

    @ExceptionHandler(value = org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(org.springframework.security.core.AuthenticationException e) {
        return ResponseEntity.<String>builder().code(401).message("未登录或token已过期").build();
    }

    @ExceptionHandler(value = org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException e) {
        return ResponseEntity.<String>builder().code(403).message("没有相关权限").build();
    }

    public @ExceptionHandler(value = Exception.class) ResponseEntity<String> handleException(Exception e) {
        log.error("系统发生异常: {}", e.getMessage(), e);
        return ResponseEntity.<String>builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).message("服务器内部错误: " + e.getMessage()).build();
    }
}
