package com.blacklabelops.crow.discover;

import java.util.Map;
import java.util.Optional;

import com.blacklabelops.crow.config.Global;
import com.cronutils.utils.StringUtils;

class GlobalExtractor {
	
	private final String prefix;
	
	public GlobalExtractor(String prefix) {
		super();
		this.prefix = prefix;
	}
	
	public Optional<Global> extractGlobalFromEnvironmentVariables(Map<String, String> environmentVariables) {
		if (checkGlobalEnvironments(environmentVariables)) {
			return Optional.of(fillGlobalFromEnvironmentVariables(environmentVariables));
		}
		return Optional.empty();
	}
	
	private Global fillGlobalFromEnvironmentVariables(Map<String, String> environmentVariables) {
		Global global = new Global();
		global.setErrorMode(environmentVariables.get(getEnvironmentField(GlobalField.ERROR_MODE)));
		global.setExecution(environmentVariables.get(getEnvironmentField(GlobalField.EXECUTION_MODE)));
		global.setShellCommand(environmentVariables.get(getEnvironmentField(GlobalField.SHELL_COMMAND)));
		global.setWorkingDirectory(environmentVariables.get(getEnvironmentField(GlobalField.WORKING_DIRECTORY)));
		return global;
	}
	
	private Global fillGlobalFromProperties(Map<String, String> properties) {
		Global global = new Global();
		global.setErrorMode(properties.get(getPropertyField(GlobalField.ERROR_MODE)));
		global.setExecution(properties.get(getPropertyField(GlobalField.EXECUTION_MODE)));
		global.setShellCommand(properties.get(getPropertyField(GlobalField.SHELL_COMMAND)));
		global.setWorkingDirectory(properties.get(getPropertyField(GlobalField.WORKING_DIRECTORY)));
		return global;
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
		return StringUtils.isEmpty(this.prefix) ? field.getEnvironmentName() : this.prefix.concat(field.getEnvironmentName());
	}

	public Optional<Global> extractGlobalFromProperties(Map<String, String> properties) {
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
