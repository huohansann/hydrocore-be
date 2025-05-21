package com.siact.common.validated;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = StringContainsValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface StringContains {
    String message() default "";
    String[] limitValues() default {};
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
