package com.blacklabelops.crow.config;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Created by steffenbleul on 29.12.16.
 */
@Documented
@Constraint(validatedBy = UniqueJobNamesValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueJobNames {

    String message() default "{UniqueJobNames}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
