package com.blacklabelops.crow.executor.console;

import com.blacklabelops.crow.executor.ExecutionMode;

import java.util.List;
import java.util.Map;

public interface IJobDefinition {

    public List<String> getCommand();

    public void setCommand(List<String> command);

    public void setCommand(String... command);

    public Map<String, String> getEnvironmentVariables();

    public void setEnvironmentVariables(Map<String, String> environmentVariables);

    public ExecutionMode getExecutionMode();

    public void setExecutionMode(ExecutionMode executorMode);

    public String getJobName();

    public void setJobName(String jobName);
}