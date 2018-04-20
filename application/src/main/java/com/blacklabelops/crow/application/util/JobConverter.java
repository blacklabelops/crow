package com.blacklabelops.crow.application.util;

import java.util.Map;
import java.util.Optional;

import org.apache.tools.ant.types.Commandline;

import com.blacklabelops.crow.application.config.JobConfiguration;
import com.blacklabelops.crow.console.definition.ErrorMode;
import com.blacklabelops.crow.console.definition.ExecutionMode;

public class JobConverter {

	private final Optional<GlobalCrowConfiguration> global;

	public JobConverter(GlobalCrowConfiguration globalConfiguration) {
		super();
		if (globalConfiguration != null) {
			global = Optional.of(globalConfiguration);
		} else {
			global = Optional.empty();
		}
	}

	public CrowConfiguration convertJob(JobConfiguration jobConfiguration) {
		return null;
	}

	public CrowConfiguration convertJob(CrowConfiguration jobConfiguration) {

		String shellCommand = evaluateShellCommand(jobConfiguration);

		CrowConfiguration job = CrowConfiguration.builder()
				.shellCommand(Optional.ofNullable(shellCommand)) //
				.jobName(jobConfiguration.getJobName()) //
				.cron(jobConfiguration.getCron()) //
				.containerName(jobConfiguration.getContainerName()) //
				.containerId(jobConfiguration.getContainerId()) //
				.timeOutMinutes(jobConfiguration.getTimeOutMinutes()) //
				.environments(Optional.ofNullable(evaluateEnvironmentVariables(jobConfiguration))) //
				.command(Optional.ofNullable(evaluateCommand(jobConfiguration, shellCommand))) //
				.preCommand(Optional.ofNullable(evaluatePreCommand(jobConfiguration,
						shellCommand))) //
				.postCommand(Optional.ofNullable(evaluatePostCommand(jobConfiguration,
						shellCommand))) //
				.workingDirectory(Optional.ofNullable(evaluateWorkingDirectory(jobConfiguration))) //
				.execution(evaluateExecutionMode(jobConfiguration)) //
				.errorMode(evaluateErrorMode(jobConfiguration)).build();

		return job;
	}

	private String evaluateErrorMode(CrowConfiguration jobConfiguration) {
		Optional<ErrorMode> mode = null;
		if (jobConfiguration.getErrorMode().isPresent()) {
			mode = Optional.of(ErrorMode.getMode(jobConfiguration.getErrorMode().get()));
		} else if (global.isPresent() && global.get().getErrorMode().isPresent()) {
			mode = Optional.of(ErrorMode.getMode(global.get().getErrorMode().get()));
		} else {
			mode = Optional.of(ErrorMode.CONTINUE);
		}
		if (mode.isPresent()) {
			return mode.get().toString().toLowerCase();
		}
		return null;
	}

	private String evaluateExecutionMode(CrowConfiguration jobConfiguration) {
		Optional<ExecutionMode> mode = null;
		if (jobConfiguration.getExecution().isPresent()) {
			mode = Optional.of(ExecutionMode.getMode(jobConfiguration.getExecution().get()));
		} else if (global.isPresent() && global.get().getExecution().isPresent()) {
			mode = Optional.of(ExecutionMode.getMode(global.get().getExecution().get()));
		} else {
			mode = Optional.of(ExecutionMode.SEQUENTIAL);
		}
		if (mode.isPresent()) {
			return mode.get().toString().toLowerCase();
		}
		return null;
	}

	private Map<String, String> evaluateEnvironmentVariables(CrowConfiguration jobConfiguration) {
		Map<String, String> envVariables = null;
		if (jobConfiguration.getEnvironments().isPresent() && !jobConfiguration.getEnvironments().get().isEmpty()) {
			envVariables = jobConfiguration.getEnvironments().get();
		}
		if (this.global.isPresent()) {
			if (global.get().getEnvironments().isPresent() && !global.get().getEnvironments().get().isEmpty()) {
				Map<String, String> environments = global.get().getEnvironments().get();
				if (envVariables != null) {
					if (envVariables != null) {
						envVariables.putAll(environments);
					}
				} else {
					envVariables = environments;
				}
			}
		}

		return envVariables;
	}

	private String evaluatePostCommand(CrowConfiguration jobConfiguration, String shellCommand) {
		if (jobConfiguration.getPostCommand().isPresent()) {
			return takeOverCommand(jobConfiguration.getPostCommand().get(), shellCommand);
		}
		return null;
	}

	private String evaluatePreCommand(CrowConfiguration jobConfiguration, String shellCommand) {
		if (jobConfiguration.getPreCommand().isPresent()) {
			return takeOverCommand(jobConfiguration.getPreCommand().get(), shellCommand);
		}
		return null;
	}

	private String evaluateCommand(CrowConfiguration jobConfiguration, String shellCommand) {
		return takeOverCommand(jobConfiguration.getCommand().orElse(null), shellCommand);
	}

	private String evaluateWorkingDirectory(CrowConfiguration jobConfiguration) {
		String workingDir = null;
		if (jobConfiguration.getWorkingDirectory().isPresent()) {
			workingDir = jobConfiguration.getWorkingDirectory().get();
		} else {
			if (global.isPresent() && global.get().getWorkingDirectory().isPresent()) {
				workingDir = global.get().getWorkingDirectory().get();
			}
		}
		return workingDir;
	}

	private String takeOverCommand(String command, String shellCommand) {
		Commandline commandLine = null;
		if (shellCommand != null) {
			commandLine = new Commandline(shellCommand);
			commandLine.addArguments(new String[] { command });
		} else if (command != null) {
			commandLine = new Commandline(command);
		}
		if (commandLine != null) {
			return String.join(" ", commandLine.getCommandline());
		} else {
			return null;
		}
	}

	private String evaluateShellCommand(CrowConfiguration jobConfiguration) {
		String shellCommand = null;
		if (jobConfiguration.getShellCommand().isPresent()) {
			shellCommand = jobConfiguration.getShellCommand().get();
		} else {
			if (global.isPresent() && global.get().getShellCommand().isPresent()) {
				shellCommand = global.get().getShellCommand().get();
			}
		}
		return shellCommand;
	}

}
