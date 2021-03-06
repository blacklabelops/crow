package com.blacklabelops.crow.application.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;

import com.cronutils.utils.StringUtils;

public class JobConfiguration implements IConfigModel {

	private String id;

	private String name;

	private String cron;

	private String command;

	private String preCommand;

	private String postCommand;

	private String shellCommand;

	private String workingDirectory;

	private String execution;

	private String errorMode;

	private String containerId;

	private String containerName;

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public String resolveJobId() {
		String jobId = getName();
		if (!StringUtils.isEmpty(getContainerId())) {
			jobId = jobId.concat("#").concat(getContainerId());
		}
		if (!StringUtils.isEmpty(getContainerName())) {
			jobId = jobId.concat("#").concat(getContainerName());
		}
		return jobId;
	}

	@Override
	public String toString() {
		return String.format(
				"JobConfiguration [name=%s, cron=%s, command=%s, preCommand=%s, postCommand=%s, shellCommand=%s, workingDirectory=%s, execution=%s, errorMode=%s, containerId=%s, containerName=%s, timeOutMinutes=%s, environments=%s]",
				name, cron, command, preCommand, postCommand, shellCommand, workingDirectory, execution, errorMode,
				containerId, containerName, timeOutMinutes, environments);
	}

}
