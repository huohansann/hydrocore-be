package com.siact.hydrocore.common.validated;

import com.github.xiaoymin.knife4j.core.util.StrUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class StringContainsValidator implements ConstraintValidator<StringContains, String> {
    private Set<String> limitValues;

    @Override
    public void initialize (StringContains constraintAnnotation) {
        limitValues = Arrays.stream(constraintAnnotation.limitValues()).map(String::toUpperCase).collect(Collectors.toSet());
    }

    @Override
    public boolean isValid (String value, ConstraintValidatorContext context) {
        if (StrUtil.isBlank(value)) {
            return true;
        }
        return limitValues.contains(value.toUpperCase());
    }
}