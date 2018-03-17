package com.blacklabelops.crow.definition;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.blacklabelops.crow.executor.ErrorMode;
import com.blacklabelops.crow.executor.ExecutionMode;


public class JobDefinition implements IJobDefinition {

    private List<String> command;
    
    private List<String> preCommand;
    
    private List<String> postCommand;

    private Map<String, String> environmentVariables;

    private ExecutionMode executorMode = null;

    private ErrorMode errorMode = null;

    private String jobName;

    private File workingDir;
    
    private Integer timeoutMinutes;

    public JobDefinition() {
        super();
    }

    @Override
    public List<String> getCommand() {
        return command;
    }

    @Override
    public void setCommand(List<String> command) {
        this.command = command;
    }

    @Override
    public void setCommand(String... command) {
        this.command = new ArrayList<String>(Arrays.asList(command));
    }
    
    @Override
    public List<String> getPreCommand() {
        return preCommand;
    }

    @Override
    public void setPreCommand(List<String> command) {
        this.preCommand = command;
    }

    @Override
    public void setPreCommand(String... command) {
        this.preCommand = new ArrayList<String>(Arrays.asList(command));
    }
    
    @Override
    public List<String> getPostCommand() {
        return postCommand;
    }

    @Override
    public void setPostCommand(List<String> command) {
        this.postCommand = command;
    }

    @Override
    public void setPostCommand(String... command) {
        this.postCommand = new ArrayList<String>(Arrays.asList(command));
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    @Override
    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    @Override
    public ExecutionMode getExecutionMode() {
        return executorMode;
    }

    @Override
    public void setExecutionMode(ExecutionMode executorMode) {
        this.executorMode = executorMode;
    }

    @Override
    public String getJobName() {
        return jobName;
    }

    @Override
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public ErrorMode getErrorMode() {
        return errorMode;
    }

    @Override
    public void setErrorMode(ErrorMode errorMode) {
        this.errorMode = errorMode;
    }

    @Override
    public File getWorkingDir() {
        return workingDir;
    }

    @Override
    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }
    
    @Override
	public Integer getTimeoutMinutes() {
		return timeoutMinutes;
	}
    
    @Override
	public void setTimeoutMinutes(Integer minutes) {
		this.timeoutMinutes = minutes;
	}
    
    

}
