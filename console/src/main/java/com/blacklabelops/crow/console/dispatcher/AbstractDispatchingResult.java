package com.blacklabelops.crow.console.dispatcher;

import java.time.LocalDateTime;

import org.immutables.value.Value;

import com.blacklabelops.crow.console.ImmutableStyle;
import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.definition.JobId;

@Value.Immutable
@ImmutableStyle
public abstract class AbstractDispatchingResult {

	@Value.Parameter(order = 1)
	public abstract Job getJobDefinition();

	@Value.Derived
	public String getJobName() {
		return getJobDefinition().getName();
	}

	@Value.Derived
	public JobId getJobId() {
		return getJobDefinition().getId();
	}

	@Value.Parameter(order = 2)
	public abstract DispatcherResult getDispatcherResult();

	@Value.Lazy
	public LocalDateTime getDispatchingTime() {
		return LocalDateTime.now();
	}

}
