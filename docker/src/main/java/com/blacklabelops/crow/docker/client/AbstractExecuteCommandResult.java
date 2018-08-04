package com.blacklabelops.crow.docker.client;

import java.util.Optional;

import org.immutables.value.Value;

@Value.Immutable
@ImmutableStyle
public abstract class AbstractExecuteCommandResult {
	
	public abstract Optional<Integer> getReturnCode();
	
	public abstract boolean isTimedOut();
}
