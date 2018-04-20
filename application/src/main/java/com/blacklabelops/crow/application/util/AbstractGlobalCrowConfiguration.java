package com.blacklabelops.crow.application.util;

import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.blacklabelops.crow.console.ImmutableStyle;

@Value.Immutable
@ImmutableStyle
public abstract class AbstractGlobalCrowConfiguration {

	public abstract Optional<String> getShellCommand();

	public abstract Optional<String> getWorkingDirectory();

	public abstract Optional<String> getExecution();

	public abstract Optional<String> getErrorMode();

	public abstract Optional<Map<String, String>> getEnvironments();
}
