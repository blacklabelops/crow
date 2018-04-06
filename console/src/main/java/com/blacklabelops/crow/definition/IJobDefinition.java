package com.blacklabelops.crow.definition;

import com.blacklabelops.crow.executor.ErrorMode;
import com.blacklabelops.crow.executor.ExecutionMode;

import java.io.File;
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

    public ErrorMode getErrorMode();

    public void setErrorMode(ErrorMode errorMode);

    public File getWorkingDir();

    public void setWorkingDir(File workingDir);

    public Integer getTimeoutMinutes();

    public void setTimeoutMinutes(Integer minutes);

    public List<String> getPreCommand();

    public void setPreCommand(List<String> command);
	
	public void setPreCommand(String... command);

	public List<String> getPostCommand();

	public void setPostCommand(List<String> command);

	public void setPostCommand(String... command);

	String getCron();

	void setCron(String cron);
}
