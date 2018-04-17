package com.blacklabelops.crow.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = UniqueJobNamesValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueJobNames {

	String message() default "{ Job Ids have to be unique! JobId = Jobname + Container Id + Container Name}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
