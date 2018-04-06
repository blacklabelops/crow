package com.blacklabelops.crow.repository;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.types.Commandline;

import com.blacklabelops.crow.config.JobConfiguration;
import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.executor.ErrorMode;
import com.blacklabelops.crow.executor.ExecutionMode;

class JobConverter {

	public JobConverter() {
		super();
	}
	
	public JobDefinition convertJob(JobConfiguration jobConfiguration) {
		JobDefinition jobDefinition = new JobDefinition();
		jobDefinition.setJobName(jobConfiguration.getName());
		jobDefinition.setCron(jobConfiguration.getCron());
        evaluateCommand(jobConfiguration, jobDefinition);
        evaluatePreCommand(jobConfiguration, jobDefinition);
        evaluatePostCommand(jobConfiguration, jobDefinition);
        evaluateWorkingDirectory(jobConfiguration, jobDefinition);
        evaluateEnvironmentVariables(jobConfiguration, jobDefinition);
        evaluateTimeout(jobConfiguration, jobDefinition);
        evaluateExecutionMode(jobConfiguration, jobDefinition);
        evaluateErrorMode(jobConfiguration, jobDefinition);
		return jobDefinition;
	}

	private void evaluateErrorMode(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		takeOverErrorMode(jobConfiguration, jobDefinition);
	}

	private void evaluateExecutionMode(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		jobDefinition.setExecutionMode(ExecutionMode.getMode(jobConfiguration.getExecution()));
	}

	private void evaluateTimeout(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		jobDefinition.setTimeoutMinutes(jobConfiguration.getTimeOutMinutes());
	}

	private void evaluateEnvironmentVariables(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		if (!jobConfiguration.getEnvironments().isEmpty()) {
            jobDefinition.setEnvironmentVariables(createEnvironmentVariables(jobConfiguration.getEnvironments()));
        }
	}

	private void evaluatePostCommand(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		if (jobConfiguration.getPostCommand() != null && !jobConfiguration.getPostCommand().isEmpty()) {
        		jobDefinition.setPostCommand(takeOverCommand(jobConfiguration.getPostCommand(), jobConfiguration.getShellCommand()));
        }
	}

	private void evaluatePreCommand(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		if (jobConfiguration.getPreCommand() != null && !jobConfiguration.getPreCommand().isEmpty()) {
        		jobDefinition.setPreCommand(takeOverCommand(jobConfiguration.getPreCommand(), jobConfiguration.getShellCommand()));
        }
	}

	private void evaluateCommand(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		jobDefinition.setCommand(takeOverCommand(jobConfiguration.getCommand(), jobConfiguration.getShellCommand()));
	}

	private void evaluateWorkingDirectory(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
		if (jobConfiguration.getWorkingDirectory() != null && !jobConfiguration.getWorkingDirectory().isEmpty()) {
            File workingDirectory = new File(jobConfiguration.getWorkingDirectory());
            if (workingDirectory.exists() && workingDirectory.isDirectory()) {
                jobDefinition.setWorkingDir(workingDirectory);
            }
        }
	}
	
	private String[] takeOverCommand(String command, String shellCommand) {
        Commandline commandLine = null;
        if (shellCommand != null && !shellCommand.isEmpty()) {
            commandLine = new Commandline( shellCommand );
            commandLine.addArguments(new String[] { command });
        } else {
            commandLine = new Commandline(command);
        }
        return commandLine.getCommandline();
    }

    private void takeOverErrorMode(JobConfiguration jobConfiguration, JobDefinition jobDefinition) {
        jobDefinition.setErrorMode(ErrorMode.getMode(jobConfiguration.getErrorMode()));
    }

    private Map<String,String> createEnvironmentVariables(Map<String, String> environments) {
        Map<String, String> environmentVariables = new HashMap<>();
        environments
        		.keySet()
        		.stream().
        		forEach(key -> environmentVariables.put(key,environments.get(key) != null ? environments.get(key) : ""));
        return environmentVariables;
    }
	
	
}
