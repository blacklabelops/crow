package com.blacklabelops.crow.application.repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.tools.ant.types.Commandline;

import com.blacklabelops.crow.application.config.Global;
import com.blacklabelops.crow.application.config.JobConfiguration;
import com.blacklabelops.crow.console.definition.ErrorMode;
import com.blacklabelops.crow.console.definition.ExecutionMode;
import com.blacklabelops.crow.console.definition.Job;
import com.cronutils.utils.StringUtils;

class JobConverter {

	private final Optional<Global> global;

	private final JobIdGenerator generator = new JobIdGenerator();

	public JobConverter(Global globalConfiguration) {
		super();
		if (globalConfiguration != null) {
			global = Optional.of(globalConfiguration);
		} else {
			global = Optional.empty();
		}
	}

	public RepositoryJob convertJob(JobConfiguration jobConfiguration) {
		RepositoryJob repositoryJob = new RepositoryJob();
		repositoryJob.setJobConfiguration(new JobConfiguration(jobConfiguration));

		String shellCommand = evaluateShellCommand(jobConfiguration);

		Job job = Job.builder()
				.id(generator.generate()) //
				.shellCommand(Optional.ofNullable(shellCommand)) //
				.name(jobConfiguration.getName()) //
				.cron(jobConfiguration.getCron()) //
				.containerName(Optional.ofNullable(jobConfiguration.getContainerName())) //
				.containerId(Optional.ofNullable(jobConfiguration.getContainerId())) //
				.timeoutMinutes(Optional.ofNullable(jobConfiguration.getTimeOutMinutes())) //
				.environmentVariables(Optional.ofNullable(evaluateEnvironmentVariables(repositoryJob
						.getJobConfiguration()))) //
				.command(evaluateCommand(repositoryJob.getJobConfiguration(), shellCommand)) //
				.preCommand(Optional.ofNullable(evaluatePreCommand(repositoryJob.getJobConfiguration(), shellCommand))) //
				.postCommand(Optional.ofNullable(evaluatePostCommand(repositoryJob.getJobConfiguration(),
						shellCommand))) //
				.workingDir(Optional.ofNullable(evaluateWorkingDirectory(repositoryJob.getJobConfiguration()))) //
				.executorMode(evaluateExecutionMode(repositoryJob.getJobConfiguration())) //
				.errorMode(evaluateErrorMode(repositoryJob.getJobConfiguration())).build();

		repositoryJob.setEvaluatedConfiguration(evaluateConfiguration(job));
		repositoryJob.setJobDefinition(job);
		return repositoryJob;
	}

	private JobConfiguration evaluateConfiguration(Job jobDefinition) {
		JobConfiguration config = new JobConfiguration();
		config.setId(jobDefinition.getId().getId());
		config.setName(jobDefinition.getName());
		config.setCron(jobDefinition.getCron().orElse(null));
		config.setShellCommand(jobDefinition.getShellCommand().orElse(null));
		config.setCommand(jobDefinition.getCommand().orElse(null).stream().collect(Collectors.joining()));
		if (jobDefinition.getPreCommand().isPresent()) {
			config.setPreCommand(jobDefinition.getPreCommand().orElse(null).stream().collect(Collectors.joining()));
		}
		if (jobDefinition.getPostCommand().isPresent()) {
			config.setPostCommand(jobDefinition.getPostCommand().orElse(null).stream().collect(Collectors.joining()));
		}
		config.setWorkingDirectory(jobDefinition.getWorkingDir().orElse(null));
		config.setEnvironments(jobDefinition.getEnvironmentVariables().orElse(null));
		config.setTimeOutMinutes(jobDefinition.getTimeoutMinutes().orElse(null));
		config.setExecution(jobDefinition.getExecutorMode().name());
		config.setErrorMode(jobDefinition.getErrorMode().name());
		config.setContainerName(jobDefinition.getContainerName().orElse(null));
		config.setContainerId(jobDefinition.getContainerId().orElse(null));
		return config;
	}

	private ErrorMode evaluateErrorMode(JobConfiguration jobConfiguration) {
		ErrorMode mode = null;
		if (!StringUtils.isEmpty(jobConfiguration.getErrorMode())) {
			mode = ErrorMode.getMode(jobConfiguration.getErrorMode());
		} else if (global.isPresent()) {
			mode = ErrorMode.getMode(global.get().getErrorMode());
		} else {
			mode = ErrorMode.CONTINUE;
		}
		return mode;
	}

	private ExecutionMode evaluateExecutionMode(JobConfiguration jobConfiguration) {
		ExecutionMode mode = null;
		if (!StringUtils.isEmpty(jobConfiguration.getExecution())) {
			mode = ExecutionMode.getMode(jobConfiguration.getExecution());
		} else if (global.isPresent()) {
			mode = ExecutionMode.getMode(global.get().getExecution());
		} else {
			mode = ExecutionMode.SEQUENTIAL;
		}
		return mode;
	}

	private Map<String, String> evaluateEnvironmentVariables(JobConfiguration jobConfiguration) {
		Map<String, String> envVariables = null;
		if (jobConfiguration.getEnvironments() != null && !jobConfiguration.getEnvironments().isEmpty()) {
			envVariables = jobConfiguration.getEnvironments();
		}
		if (this.global.isPresent()) {
			if (global.get().getEnvironments() != null && !global.get().getEnvironments().isEmpty()) {
				Map<String, String> environments = global.get().getEnvironments();
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

	private List<String> evaluatePostCommand(JobConfiguration jobConfiguration, String shellCommand) {
		if (jobConfiguration.getPostCommand() != null && !jobConfiguration.getPostCommand().isEmpty()) {
			return takeOverCommand(jobConfiguration.getPostCommand(), shellCommand);
		}
		return null;
	}

	private List<String> evaluatePreCommand(JobConfiguration jobConfiguration, String shellCommand) {
		if (jobConfiguration.getPreCommand() != null && !jobConfiguration.getPreCommand().isEmpty()) {
			return takeOverCommand(jobConfiguration.getPreCommand(), shellCommand);
		}
		return null;
	}

	private List<String> evaluateCommand(JobConfiguration jobConfiguration, String shellCommand) {
		return takeOverCommand(jobConfiguration.getCommand(), shellCommand);
	}

	private String evaluateWorkingDirectory(JobConfiguration jobConfiguration) {
		String workingDir = null;
		if (!StringUtils.isEmpty(jobConfiguration.getWorkingDirectory())) {
			workingDir = jobConfiguration.getWorkingDirectory();
		} else {
			if (global.isPresent()) {
				if (!StringUtils.isEmpty(global.get().getWorkingDirectory())) {
					workingDir = global.get().getWorkingDirectory();
				}
			}
		}
		return workingDir;
	}

	private List<String> takeOverCommand(String command, String shellCommand) {
		Commandline commandLine = null;
		if (!StringUtils.isEmpty(shellCommand)) {
			commandLine = new Commandline(shellCommand);
			commandLine.addArguments(new String[] { command });
		} else {
			commandLine = new Commandline(command);
		}
		return Arrays.asList(commandLine.getCommandline());
	}

	private String evaluateShellCommand(JobConfiguration jobConfiguration) {
		String shellCommand = null;
		if (!StringUtils.isEmpty(jobConfiguration.getShellCommand())) {
			shellCommand = jobConfiguration.getShellCommand();
		} else {
			if (global.isPresent()) {
				if (!StringUtils.isEmpty(global.get().getShellCommand())) {
					shellCommand = global.get().getShellCommand();
				}
			}
		}
		return shellCommand;
	}

}
