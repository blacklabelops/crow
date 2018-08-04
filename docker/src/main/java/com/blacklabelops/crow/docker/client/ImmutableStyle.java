package com.blacklabelops.crow.docker.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Target({ ElementType.PACKAGE, ElementType.TYPE })
@Retention(RetentionPolicy.CLASS) // Make it class retention for incremental compilation
@Value.Style(get = { "is*", "get*" }, // Detect 'get' and 'is' prefixes in accessor methods
		visibility = ImplementationVisibility.PUBLIC, // Generated class will be always public
		depluralize = true, // enable feature
		defaultAsDefault = true, //
		typeAbstract = { "Abstract*" }, // 'Abstract' prefix will be detected and trimmed
		typeImmutable = "*", // No prefix or suffix for generated immutable type
		deepImmutablesDetection = true)
public @interface ImmutableStyle {

}
