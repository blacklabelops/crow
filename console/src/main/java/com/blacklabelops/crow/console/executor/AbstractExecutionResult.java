package com.blacklabelops.crow.console.executor;

import java.time.LocalDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import com.blacklabelops.crow.console.ImmutableStyle;
import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.definition.JobId;

@Value.Immutable
@ImmutableStyle
public abstract class AbstractExecutionResult {

	@Value.Parameter(order = 1)
	public abstract Job getJobDefinition();

	@Value.Derived
	public JobId getJobId() {
		return getJobDefinition().getId();
	}

	public abstract Optional<Integer> getReturnCode();

	public abstract Optional<Boolean> isTimedOut();

	public abstract Optional<LocalDateTime> getStartingTime();

	public abstract Optional<LocalDateTime> getFinishingTime();

	public abstract Optional<LocalDateTime> getErrorTime();

}
