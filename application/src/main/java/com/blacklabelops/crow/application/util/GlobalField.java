package com.blacklabelops.crow.application.util;

enum GlobalField {
	
	SHELL_COMMAND("SHELL_COMMAND","shell.command"),
	WORKING_DIRECTORY("WORKING_DIRECTORY","working.directory"),
	EXECUTION_MODE("EXECUTION","execution"),
	ERROR_MODE("ON_ERROR","on.error");
	
	private final String environmentName;
	
	private final String propertyName;
	
	private GlobalField(String environmentName, String propertyName) {
		this.environmentName = environmentName;
		this.propertyName = propertyName;
	}

	public String getEnvironmentName() {
		return environmentName;
	}

	public String getPropertyName() {
		return propertyName;
	}
}
