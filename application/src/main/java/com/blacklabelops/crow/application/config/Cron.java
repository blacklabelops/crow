package com.blacklabelops.crow.application.config;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;


@Documented
@Constraint(validatedBy = CronValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Cron {

    String message() default "{Cron}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
