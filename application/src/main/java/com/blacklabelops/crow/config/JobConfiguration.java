package com.blacklabelops.crow.config;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.BeanUtils;


public class JobConfiguration implements IConfigModel {

    @NotEmpty(message = "A unique name for each job has to be defined!")
    private String name;

    @Cron(message = "Cron expression must be valid!")
    private String cron;

    @NotEmpty(message = "Your command is not allowed to be empty!")
    private String command;
    
    private String preCommand;
    
    private String postCommand;
    
    private String shellCommand;

    private String workingDirectory;

    private String execution;

    private String errorMode;
    
    private Integer timeOutMinutes;

    private Map<String, String> environments = new HashMap<>();
    
    public JobConfiguration() {
        super();
    }
    
    public JobConfiguration(JobConfiguration anotherConfiguration) {
    	 	super();
    	 	BeanUtils.copyProperties(anotherConfiguration, this);
    	 	if (anotherConfiguration.getEnvironments() != null) {
    	 		this.environments = new HashMap<>(anotherConfiguration.getEnvironments());
    	 	} else {
    	 		this.environments = null;
    	 	}
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, String> getEnvironments() {
    		return environments;
    }

    public void setEnvironments(Map<String, String> environments) {
    		this.environments = environments;
    }

    public String getExecution() {
        return execution;
    }

    public void setExecution(String execution) {
        this.execution = execution;
    }

    public String getErrorMode() {
        return errorMode;
    }

    public void setErrorMode(String errorMode) {
        this.errorMode = errorMode;
    }

    public String getShellCommand() {
        return shellCommand;
    }

    public void setShellCommand(String shellCommand) {
        this.shellCommand = shellCommand;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

	public String getPreCommand() {
		return preCommand;
	}

	public void setPreCommand(String preCommand) {
		this.preCommand = preCommand;
	}

	public String getPostCommand() {
		return postCommand;
	}

	public void setPostCommand(String postCommand) {
		this.postCommand = postCommand;
	}

	public Integer getTimeOutMinutes() {
		return timeOutMinutes;
	}

	public void setTimeOutMinutes(Integer timeOutMinutes) {
		this.timeOutMinutes = timeOutMinutes;
	}

	@Override
	public String toString() {
		return String.format(
				"JobConfiguration\n[ name=%s\n, cron=%s\n, command=%s\n, preCommand=%s\n, postCommand=%s\n, shellCommand=%s\n, workingDirectory=%s\n, execution=%s\n, errorMode=%s\n, timeOutMinutes=%s\n, environments=%s]",
				name, cron, command, preCommand, postCommand, shellCommand, workingDirectory, execution, errorMode,
				timeOutMinutes, environments);
	}
    
}
