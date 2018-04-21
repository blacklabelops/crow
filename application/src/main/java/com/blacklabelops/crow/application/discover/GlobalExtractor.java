package com.blacklabelops.crow.application.discover;

import java.util.Map;
import java.util.Optional;

import com.blacklabelops.crow.application.util.GlobalCrowConfiguration;
import com.cronutils.utils.StringUtils;

public class GlobalExtractor {

	private final String prefix;

	public GlobalExtractor(String prefix) {
		super();
		this.prefix = prefix;
	}

	public Optional<GlobalCrowConfiguration> extractGlobalFromEnvironmentVariables(
			Map<String, String> environmentVariables) {
		if (checkGlobalEnvironments(environmentVariables)) {
			return Optional.of(fillGlobalFromEnvironmentVariables(environmentVariables));
		}
		return Optional.empty();
	}

	private GlobalCrowConfiguration fillGlobalFromEnvironmentVariables(Map<String, String> environmentVariables) {
		return GlobalCrowConfiguration.builder() //
				.errorMode(emptyString(environmentVariables.get(getEnvironmentField(GlobalField.ERROR_MODE)))) //
				.execution(emptyString(environmentVariables.get(getEnvironmentField(
						GlobalField.EXECUTION_MODE)))) //
				.shellCommand(emptyString(environmentVariables.get(getEnvironmentField(
						GlobalField.SHELL_COMMAND)))) //
				.workingDirectory(emptyString(environmentVariables.get(getEnvironmentField(
						GlobalField.WORKING_DIRECTORY)))).build();
	}

	private GlobalCrowConfiguration fillGlobalFromProperties(Map<String, String> properties) {
		return GlobalCrowConfiguration.builder()
				.errorMode(emptyString(properties.get(getPropertyField(GlobalField.ERROR_MODE))))
				.execution(emptyString(properties.get(getPropertyField(GlobalField.EXECUTION_MODE))))
				.shellCommand(emptyString(properties.get(getPropertyField(GlobalField.SHELL_COMMAND))))
				.workingDirectory(emptyString(properties.get(getPropertyField(GlobalField.WORKING_DIRECTORY))))
				.build();
	}

	public Optional<String> emptyString(String string) {
		String result = null;
		if (!StringUtils.isEmpty(string)) {
			result = string;
		}
		return Optional.ofNullable(result);
	}

	private boolean checkGlobalEnvironments(Map<String, String> environmentVariables) {
		boolean exists = false;
		for (GlobalField field : GlobalField.values()) {
			String checkedField = getEnvironmentField(field);
			if (environmentVariables.containsKey(checkedField)) {
				exists = true;
			}
		}
		return exists;
	}

	private String getEnvironmentField(GlobalField field) {
		return StringUtils.isEmpty(this.prefix) ? field.getEnvironmentName()
				: this.prefix.concat(field.getEnvironmentName());
	}

	public Optional<GlobalCrowConfiguration> extractGlobalFromProperties(Map<String, String> properties) {
		if (checkGlobalProperties(properties)) {
			return Optional.of(fillGlobalFromProperties(properties));
		}
		return Optional.empty();
	}

	private boolean checkGlobalProperties(Map<String, String> properties) {
		boolean exists = false;
		for (GlobalField field : GlobalField.values()) {
			String checkedField = getPropertyField(field);
			if (properties.containsKey(checkedField)) {
				exists = true;
			}
		}
		return exists;
	}

	private String getPropertyField(GlobalField field) {
		return StringUtils.isEmpty(this.prefix) ? field.getPropertyName() : this.prefix.concat(field.getPropertyName());
	}
}
