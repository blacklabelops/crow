package com.blacklabelops.crow.application.discover;

import java.util.Optional;

import com.blacklabelops.crow.application.config.Global;
import com.blacklabelops.crow.application.config.JobConfiguration;
import com.blacklabelops.crow.application.model.CrowConfiguration;
import com.blacklabelops.crow.application.model.GlobalCrowConfiguration;
import com.cronutils.utils.StringUtils;

public class JobConfigurationConverter {

	public JobConfigurationConverter() {
		super();
	}

	public CrowConfiguration convertJob(JobConfiguration jobConfiguration) {
		return CrowConfiguration.builder()
				.shellCommand(emptyString(jobConfiguration.getShellCommand())) //
				.jobName(emptyString(jobConfiguration.getName())) //
				.cron(emptyString(jobConfiguration.getCron())) //
				.containerName(emptyString(jobConfiguration.getContainerName())) //
				.containerId(emptyString(jobConfiguration.getContainerId())) //
				.timeOutMinutes(Optional.ofNullable(jobConfiguration.getTimeOutMinutes())) //
				.environments(Optional.ofNullable(jobConfiguration.getEnvironments())) //
				.command(emptyString(jobConfiguration.getCommand())) //
				.preCommand(emptyString(jobConfiguration.getPreCommand())) //
				.postCommand(emptyString(jobConfiguration.getPostCommand()))
				.workingDirectory(emptyString(jobConfiguration.getWorkingDirectory())) //
				.execution(emptyString(jobConfiguration.getExecution())) //
				.errorMode(emptyString(jobConfiguration.getErrorMode())).build();
	}

	public Optional<String> emptyString(String string) {
		String result = null;
		if (!StringUtils.isEmpty(string)) {
			result = string;
		}
		return Optional.ofNullable(result);
	}

	public GlobalCrowConfiguration convertGlobal(Global globalConfiguration) {
		return GlobalCrowConfiguration.builder() //
				.errorMode(emptyString(globalConfiguration.getErrorMode())) //
				.execution(emptyString(globalConfiguration.getExecution())) //
				.shellCommand(emptyString(globalConfiguration.getShellCommand())) //
				.workingDirectory(emptyString(globalConfiguration.getWorkingDirectory()))
				.environments(Optional.ofNullable(globalConfiguration.getEnvironments()))
				.build();
	}
}
