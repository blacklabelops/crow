package com.blacklabelops.crow.application.model;

import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.blacklabelops.crow.console.ImmutableStyle;

@Value.Immutable
@ImmutableStyle
public abstract class AbstractCrowConfiguration {

	public abstract Optional<String> getJobId();

	public abstract Optional<String> getJobName();

	public abstract Optional<String> getCron();

	public abstract Optional<String> getCommand();

	public abstract Optional<String> getPreCommand();

	public abstract Optional<String> getPostCommand();

	public abstract Optional<String> getShellCommand();

	public abstract Optional<String> getWorkingDirectory();

	public abstract Optional<String> getExecution();

	public abstract Optional<String> getErrorMode();

	public abstract Optional<String> getContainerId();

	public abstract Optional<String> getContainerName();

	public abstract Optional<Integer> getTimeOutMinutes();

	public abstract Optional<Map<String, String>> getEnvironments();

}
