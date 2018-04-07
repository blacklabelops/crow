package com.blacklabelops.crow.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;


public class JobDescription {

    private String name;

    private String cron;

    private String command;
    
    private String preCommand;
    
    private String postCommand;
    
    private String shellCommand;

    private String workingDirectory;

    private String execution;

    private String errorMode;
    
    private Integer timeOutMinutes;

    private Map<String, String> environments = new HashMap<>();

    public JobDescription() {
        super();
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
    
}
