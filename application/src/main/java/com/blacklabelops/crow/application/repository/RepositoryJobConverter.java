package com.blacklabelops.crow.application.repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.tools.ant.types.Commandline;

import com.blacklabelops.crow.application.model.CrowConfiguration;
import com.blacklabelops.crow.console.definition.ErrorMode;
import com.blacklabelops.crow.console.definition.ExecutionMode;
import com.blacklabelops.crow.console.definition.Job;
import com.cronutils.utils.StringUtils;

class RepositoryJobConverter {

	private JobIdGenerator generator = new JobIdGenerator();

	public RepositoryJobConverter() {
		super();
	}

	public RepositoryJob convertJob(CrowConfiguration jobConfiguration) {
		RepositoryJob repositoryJob = new RepositoryJob();

		String shellCommand = jobConfiguration.getShellCommand().orElse(null);

		Job job = Job.builder()
				.id(generator.generate()) //
				.shellCommand(Optional.ofNullable(shellCommand)) //
				.name(jobConfiguration.getJobName().orElse(null)) //
				.cron(jobConfiguration.getCron()) //
				.containerName(jobConfiguration.getContainerName()) //
				.containerId(jobConfiguration.getContainerId()) //
				.timeoutMinutes(jobConfiguration.getTimeOutMinutes()) //
				.environmentVariables(Optional.ofNullable(evaluateEnvironmentVariables(jobConfiguration))) //
				.command(Optional.ofNullable(evaluateCommand(jobConfiguration, shellCommand))) //
				.preCommand(Optional.ofNullable(evaluatePreCommand(jobConfiguration, shellCommand))) //
				.postCommand(Optional.ofNullable(evaluatePostCommand(jobConfiguration,
						shellCommand))) //
				.workingDir(Optional.ofNullable(evaluateWorkingDirectory(jobConfiguration))) //
				.executorMode(evaluateExecutionMode(jobConfiguration)) //
				.errorMode(evaluateErrorMode(jobConfiguration)).build();
		repositoryJob.setJobDefinition(job);
		repositoryJob.setJobConfiguration(jobConfiguration.withJobId(job.getId().getId()));
		return repositoryJob;
	}

	private ErrorMode evaluateErrorMode(CrowConfiguration jobConfiguration) {
		ErrorMode mode = null;
		if (jobConfiguration.getErrorMode().isPresent()) {
			mode = ErrorMode.getMode(jobConfiguration.getErrorMode().get());
		} else {
			mode = ErrorMode.CONTINUE;
		}
		return mode;
	}

	private ExecutionMode evaluateExecutionMode(CrowConfiguration jobConfiguration) {
		ExecutionMode mode = null;
		if (jobConfiguration.getExecution().isPresent()) {
			mode = ExecutionMode.getMode(jobConfiguration.getExecution().get());
		} else {
			mode = ExecutionMode.SEQUENTIAL;
		}
		return mode;
	}

	private Map<String, String> evaluateEnvironmentVariables(CrowConfiguration jobConfiguration) {
		Map<String, String> envVariables = null;
		if (jobConfiguration.getEnvironments().isPresent() && !jobConfiguration.getEnvironments().get().isEmpty()) {
			envVariables = jobConfiguration.getEnvironments().get();
		}

		return envVariables;
	}

	private List<String> evaluatePostCommand(CrowConfiguration jobConfiguration, String shellCommand) {
		if (jobConfiguration.getPostCommand().isPresent()) {
			return takeOverCommand(jobConfiguration.getPostCommand().get(), shellCommand);
		}
		return null;
	}

	private List<String> evaluatePreCommand(CrowConfiguration jobConfiguration, String shellCommand) {
		if (jobConfiguration.getPreCommand().isPresent()) {
			return takeOverCommand(jobConfiguration.getPreCommand().get(), shellCommand);
		}
		return null;
	}

	private List<String> evaluateCommand(CrowConfiguration jobConfiguration, String shellCommand) {
		return takeOverCommand(jobConfiguration.getCommand().get(), shellCommand);
	}

	private String evaluateWorkingDirectory(CrowConfiguration jobConfiguration) {
		String workingDir = null;
		if (jobConfiguration.getWorkingDirectory().isPresent()) {
			workingDir = jobConfiguration.getWorkingDirectory().get();
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

}
