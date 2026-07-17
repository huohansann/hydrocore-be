package com.siact.hydrocore.common.exception;

import com.siact.hydrocore.common.api.ApiResponse;
import com.siact.hydrocore.common.api.ApiResponseCode;
import com.siact.hydrocore.common.web.TraceIdResolver;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.text.ParseException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public @ExceptionHandler(value = IOException.class) ApiResponse<String> handleIOException(IOException e, HandlerMethod method) {
        String traceId = TraceIdResolver.currentTraceId();
        log.error("io exception handled, handler={}, traceId={}, message={}", handlerName(method), traceId, e.getMessage(), e);
        return ApiResponse.fail(ApiResponseCode.INTERNAL_ERROR.getCode(), "IO 异常", traceId);
    }

    public @ExceptionHandler(value = MethodArgumentNotValidException.class) ApiResponse<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HandlerMethod method) {
        String traceId = TraceIdResolver.currentTraceId();
        BindingResult bindingResult = e.getBindingResult();
        ObjectError allErrors = bindingResult.getAllErrors().get(0);
        log.warn("validation exception handled, handler={}, traceId={}, message={}", handlerName(method), traceId, allErrors.getDefaultMessage());
        return ApiResponse.fail(ApiResponseCode.BAD_REQUEST.getCode(), allErrors.getDefaultMessage(), traceId);
    }

    public @ExceptionHandler(value = ConstraintViolationException.class) ApiResponse<String> handleConstraintViolationException(ConstraintViolationException e, HandlerMethod method) {
        String traceId = TraceIdResolver.currentTraceId();
        log.warn("constraint violation handled, handler={}, traceId={}, message={}", handlerName(method), traceId, e.getMessage());
        return ApiResponse.fail(ApiResponseCode.BAD_REQUEST.getCode(), e.getMessage(), traceId);
    }

    public @ExceptionHandler(value = ParseException.class) ApiResponse<String> handleParseException(ParseException e, HandlerMethod method) {
        String traceId = TraceIdResolver.currentTraceId();
        log.warn("parse exception handled, handler={}, traceId={}, message={}", handlerName(method), traceId, e.getMessage());
        return ApiResponse.fail(ApiResponseCode.BAD_REQUEST.getCode(), ApiResponseCode.BAD_REQUEST.getMessage(), traceId);
    }

    public @ExceptionHandler(value = BizException.class) ApiResponse<String> handleBizException(BizException e, HandlerMethod method) {
        String traceId = TraceIdResolver.currentTraceId();
        log.warn("business exception handled, handler={}, traceId={}, message={}", handlerName(method), traceId, e.getMessage());
        return ApiResponse.fail(ApiResponseCode.INTERNAL_ERROR.getCode(), e.getMessage(), traceId);
    }

    public @ExceptionHandler(value = CustomException.class) ApiResponse<String> handleCustomException(CustomException e, HandlerMethod method) {
        String traceId = TraceIdResolver.currentTraceId();
        log.warn("custom exception handled, handler={}, traceId={}, message={}", handlerName(method), traceId, e.getMessage());
        return ApiResponse.fail(ApiResponseCode.INTERNAL_ERROR.getCode(), e.getMessage(), traceId);
    }

    @ExceptionHandler(value = org.springframework.security.core.AuthenticationException.class)
    public ApiResponse<String> handleAuthenticationException(org.springframework.security.core.AuthenticationException e) {
        String traceId = TraceIdResolver.currentTraceId();
        log.warn("authentication exception handled, traceId={}, message={}", traceId, e.getMessage());
        return ApiResponse.fail(ApiResponseCode.UNAUTHORIZED.getCode(), ApiResponseCode.UNAUTHORIZED.getMessage(), traceId);
    }

    @ExceptionHandler(value = org.springframework.security.access.AccessDeniedException.class)
    public ApiResponse<String> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException e) {
        String traceId = TraceIdResolver.currentTraceId();
        log.warn("access denied exception handled, traceId={}, message={}", traceId, e.getMessage());
        return ApiResponse.fail(ApiResponseCode.FORBIDDEN.getCode(), ApiResponseCode.FORBIDDEN.getMessage(), traceId);
    }

    public @ExceptionHandler(value = Exception.class) ApiResponse<String> handleException(Exception e) {
        String traceId = TraceIdResolver.currentTraceId();
        log.error("unhandled request exception, traceId={}, exceptionType={}, message={}", traceId, e.getClass().getName(), e.getMessage(), e);
        return ApiResponse.fail(ApiResponseCode.INTERNAL_ERROR.getCode(), "服务器内部错误: " + e.getMessage(), traceId);
    }

    private String handlerName(HandlerMethod method) {
        if (method == null) {
            return "unknown";
        }
        ApiOperation operation = method.getMethodAnnotation(ApiOperation.class);
        if (operation != null) {
            return operation.value();
        }
        return method.getBeanType().getSimpleName() + "#" + method.getMethod().getName();
    }
}
