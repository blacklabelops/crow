package com.blacklabelops.crow.executor.console;

import com.blacklabelops.crow.executor.ErrorMode;
import com.blacklabelops.crow.executor.ExecutionMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by steffenbleul on 19.12.16.
 */
public class JobDefinition implements IJobDefinition {

    private List<String> command;

    private List<String> shellCommand;

    private Map<String, String> environmentVariables;

    private ExecutionMode executorMode = null;

    private ErrorMode errorMode = null;

    private String jobName;

    private File workingDir;

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

}
