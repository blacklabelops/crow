package com.blacklabelops.crow.application.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.tools.ant.types.Commandline;

import com.blacklabelops.crow.application.config.Global;
import com.blacklabelops.crow.application.config.JobConfiguration;
import com.blacklabelops.crow.console.definition.ErrorMode;
import com.blacklabelops.crow.console.definition.ExecutionMode;
import com.blacklabelops.crow.console.definition.JobDefinition;
import com.cronutils.utils.StringUtils;

class JobConverter {

	private final Optional<Global> global;

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
		repositoryJob.setJobDefinition(new JobDefinition());
		repositoryJob.getJobDefinition().setJobName(jobConfiguration.getName());
		repositoryJob.getJobDefinition().setCron(jobConfiguration.getCron());
		repositoryJob.getJobDefinition().setContainerName(jobConfiguration.getContainerName());
		repositoryJob.getJobDefinition().setContainerId(jobConfiguration.getContainerId());
		evaluateShellCommand(jobConfiguration, repositoryJob.getJobDefinition());
		evaluateCommand(repositoryJob.getJobConfiguration(), repositoryJob.getJobDefinition());
		evaluatePreCommand(repositoryJob.getJobConfiguration(), repositoryJob.getJobDefinition());
		evaluatePostCommand(repositoryJob.getJobConfiguration(), repositoryJob.getJobDefinition());
		evaluateWorkingDirectory(repositoryJob.getJobConfiguration(), repositoryJob.getJobDefinition());
		evaluateEnvironmentVariables(repositoryJob.getJobConfiguration(), repositoryJob.getJobDefinition());
		evaluateTimeout(repositoryJob.getJobConfiguration(), repositoryJob.getJobDefinition());
		evaluateExecutionMode(repositoryJob.getJobConfiguration(), repositoryJob.getJobDefinition());
		evaluateErrorMode(repositoryJob.getJobConfiguration(), repositoryJob.getJobDefinition());
		repositoryJob.setEvaluatedConfiguration(evaluateConfiguration(repositoryJob.getJobDefinition()));
		return repositoryJob;
	}

	private JobConfiguration evaluateConfiguration(JobDefinition jobDefinition) {
		JobConfiguration config = new JobConfiguration();
		config.setName(jobDefinition.getJobName());
		config.setCron(jobDefinition.getCron());
		config.setShellCommand(jobDefinition.getShellCommand());
		config.setCommand(jobDefinition.getCommand().stream().collect(Collectors.joining()));
		if (jobDefinition.getPreCommand() != null) {
			config.setPreCommand(jobDefinition.getPreCommand().stream().collect(Collectors.joining()));
		}
		if (jobDefinition.getPostCommand() != null) {
			config.setPostCommand(jobDefinition.getPostCommand().stream().collect(Collectors.joining()));
		}
		config.setWorkingDirectory(jobDefinition.getWorkingDir());
		config.setEnvironments(jobDefinition.getEnvironmentVariables());
		config.setTimeOutMinutes(jobDefinition.getTimeoutMinutes());
		config.setExecution(jobDefinition.getExecutionMode().name());
		config.setErrorMode(jobDefinition.getErrorMode().name());
		return config;
	}

	private void evaluateErrorMode(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		if (!StringUtils.isEmpty(jobConfiguration.getErrorMode())) {
			jobDefinition.setErrorMode(ErrorMode.getMode(jobConfiguration.getErrorMode()));
		} else if (global.isPresent()) {
			jobDefinition.setErrorMode(ErrorMode.getMode(global.get().getErrorMode()));
		} else {
			jobDefinition.setErrorMode(ErrorMode.CONTINUE);
		}
	}

	private void evaluateExecutionMode(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		if (!StringUtils.isEmpty(jobConfiguration.getExecution())) {
			jobDefinition.setExecutionMode(ExecutionMode.getMode(jobConfiguration.getExecution()));
		} else if (global.isPresent()) {
			jobDefinition.setExecutionMode(ExecutionMode.getMode(global.get().getExecution()));
		} else {
			jobDefinition.setExecutionMode(ExecutionMode.SEQUENTIAL);
		}
	}

	private void evaluateTimeout(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		jobDefinition.setTimeoutMinutes(jobConfiguration.getTimeOutMinutes());
	}

	private void evaluateEnvironmentVariables(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		if (jobConfiguration.getEnvironments() != null && !jobConfiguration.getEnvironments().isEmpty()) {
			jobDefinition.setEnvironmentVariables(createEnvironmentVariables(jobConfiguration.getEnvironments()));
		}
		this.global.ifPresent(g -> {
			if (g.getEnvironments() != null && !g.getEnvironments().isEmpty()) {
				Map<String, String> environments = g.getEnvironments();
				if (environments != null) {
					if (jobDefinition.getEnvironmentVariables() != null) {
						environments.putAll(jobDefinition.getEnvironmentVariables());
					}
				} else {
					environments = jobDefinition.getEnvironmentVariables();
				}
				jobDefinition.setEnvironmentVariables(environments);
			}
		});
	}

	private void evaluatePostCommand(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		if (jobConfiguration.getPostCommand() != null && !jobConfiguration.getPostCommand().isEmpty()) {
			jobDefinition.setPostCommand(takeOverCommand(jobConfiguration.getPostCommand(), jobDefinition
					.getShellCommand()));
		}
	}

	private void evaluatePreCommand(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		if (jobConfiguration.getPreCommand() != null && !jobConfiguration.getPreCommand().isEmpty()) {
			jobDefinition.setPreCommand(takeOverCommand(jobConfiguration.getPreCommand(), jobDefinition
					.getShellCommand()));
		}
	}

	private void evaluateCommand(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		jobDefinition.setCommand(takeOverCommand(jobConfiguration.getCommand(), jobDefinition.getShellCommand()));
	}

	private void evaluateWorkingDirectory(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		if (!StringUtils.isEmpty(jobConfiguration.getWorkingDirectory())) {
			jobDefinition.setWorkingDir(jobConfiguration.getWorkingDirectory());
		} else {
			global.ifPresent(g -> {
				if (!StringUtils.isEmpty(g.getWorkingDirectory())) {
					jobDefinition.setWorkingDir(g.getWorkingDirectory());
				}
			});
		}
	}

	private String[] takeOverCommand(String command, String shellCommand) {
		Commandline commandLine = null;
		if (!StringUtils.isEmpty(shellCommand)) {
			commandLine = new Commandline(shellCommand);
			commandLine.addArguments(new String[] { command });
		} else {
			commandLine = new Commandline(command);
		}
		return commandLine.getCommandline();
	}

	private void evaluateShellCommand(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		if (!StringUtils.isEmpty(jobConfiguration.getShellCommand())) {
			jobDefinition.setShellCommand(jobConfiguration.getShellCommand());
		} else {
			global.ifPresent(g -> {
				if (!StringUtils.isEmpty(g.getShellCommand())) {
					jobDefinition.setShellCommand(g.getShellCommand());
				}
			});
		}
	}

	private Map<String, String> createEnvironmentVariables(Map<String, String> environments) {
		Map<String, String> environmentVariables = new HashMap<>();
		environments
				.keySet()
				.stream().forEach(key -> environmentVariables.put(key, environments.get(key) != null ? environments.get(
						key) : ""));
		return environmentVariables;
	}

}
