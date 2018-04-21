package com.blacklabelops.crow.application.model;

import java.util.Map;
import java.util.Optional;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;
import org.immutables.value.Value;

import com.blacklabelops.crow.application.config.IConfigModel;
import com.blacklabelops.crow.console.ImmutableStyle;

@Value.Immutable
@ImmutableStyle
public abstract class AbstractCrowConfiguration implements IConfigModel {

	public abstract Optional<String> getJobId();

	@NotEmpty(message = "A job name has to be defined!")
	@Pattern(regexp = "[a-zA-Z0-9_\\.]+", message = "Only numbers and chars are allowed in job names! Regex: [a-zA-Z0-9_\\\\.]+")
	@UnwrapValidatedValue
	public abstract Optional<String> getJobName();

	@Cron(message = "Cron expression must be valid!")
	@UnwrapValidatedValue
	public abstract Optional<String> getCron();

	@NotEmpty(message = "A job command has to be defined!")
	@UnwrapValidatedValue
	public abstract Optional<String> getCommand();

	public abstract Optional<String> getPreCommand();

	public abstract Optional<String> getPostCommand();

	public abstract Optional<String> getShellCommand();

	public abstract Optional<String> getWorkingDirectory();

	public abstract Optional<String> getExecution();

	public abstract Optional<String> getErrorMode();

	public abstract Optional<String> getContainerId();

	public abstract Optional<String> getContainerName();

	@Min(value = 0L, message = "The value must be positive")
	@UnwrapValidatedValue
	public abstract Optional<Integer> getTimeOutMinutes();

	public abstract Optional<Map<String, String>> getEnvironments();

}
