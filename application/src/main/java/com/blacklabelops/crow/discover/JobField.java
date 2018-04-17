package com.blacklabelops.crow.discover;

enum JobField {

	NAME("NAME", ".name"), //
	CRON("CRON", ".cron"), //
	COMMAND("COMMAND", ".command"), //
	PRE_COMMAND("PRE_COMMAND", ".pre.command"), //
	POST_COMMAND("POST_COMMAND", ".post.command"), //
	WORKING_DIRECTORY("WORKING_DIRECTORY", ".working.directory"), //
	TIMEOUT_MINUTES("TIMEOUT_MINUTES", ".timeout.minutes"), //
	SHELL_COMMAND("SHELL_COMMAND", ".shell.command"), //
	EXECUTION_MODE("EXECUTION", ".execution"), //
	ON_ERROR("ON_ERROR", ".on.error"), //
	CONTAINER_NAME("CONTAINER_NAME", ".container.name"), //
	CONTAINER_ID("CONTAINER_ID", ".container.id");

	private final String environmentName;

	private final String propertyName;

	private JobField(String environmentName, String propertyName) {
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
