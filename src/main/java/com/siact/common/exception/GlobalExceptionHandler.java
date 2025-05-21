package com.siact.common.exception;

import com.siact.common.result.R;
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
import java.util.Objects;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    public R<String> handle(Exception e, HandlerMethod method) {
        ApiOperation operation = method.getMethodAnnotation(ApiOperation.class);
        log.error("{} error", Objects.requireNonNull(operation).value(), e);
        if (e instanceof IOException) {
            return R.fail("IO异常");
        }
        if (e instanceof MethodArgumentNotValidException) {
            BindingResult bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
            ObjectError allErrors = bindingResult.getAllErrors().get(0);
            return R.fail(allErrors.getDefaultMessage());
        }
        if (e instanceof ConstraintViolationException) {
            return R.fail(e.getMessage());
        }
        if (e instanceof ParseException) {
            return R.fail(CommonEnum.BODY_NOT_MATCH.getResultMsg());
        }
        if (e instanceof BizException) {
            return R.fail(e.getMessage());
        }
        if (e instanceof CustomException) {
            return R.fail(e.getMessage());
        }
        return R.fail(CommonEnum.INTERNAL_SERVER_ERROR.getResultMsg());
    }
}