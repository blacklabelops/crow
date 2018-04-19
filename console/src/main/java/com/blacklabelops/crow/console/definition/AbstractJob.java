package com.blacklabelops.crow.console.definition;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.blacklabelops.crow.console.ImmutableStyle;

@Value.Immutable
@ImmutableStyle
public abstract class AbstractJob {

	@Value.Parameter(order = 1)
	public abstract AbstractJobId getId();

	@Value.Parameter(order = 2)
	public abstract String getName();

	public abstract Optional<String> getCron();

	public abstract Optional<String> getShellCommand();

	public abstract Optional<List<String>> getCommand();

	public abstract Optional<List<String>> getPreCommand();

	public abstract Optional<List<String>> getPostCommand();

	public abstract Optional<Map<String, String>> getEnvironmentVariables();

	@Value.Default
	public ExecutionMode getExecutorMode() {
		return ExecutionMode.SEQUENTIAL;
	}

	@Value.Default
	public ErrorMode getErrorMode() {
		return ErrorMode.CONTINUE;
	}

	public abstract Optional<String> getWorkingDir();

	public abstract Optional<Integer> getTimeoutMinutes();

	public abstract Optional<String> getContainerId();

	public abstract Optional<String> getContainerName();

	@Value.Derived
	public String jobLabel() {
		String jobLabel = getName();
		if (getContainerName().isPresent()) {
			jobLabel.concat(" - ").concat(getContainerName().get());
		} else if (getContainerId().isPresent()) {
			jobLabel.concat(" - ").concat(getContainerId().get());
		}
		return jobLabel;
	}

}
