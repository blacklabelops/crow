package com.blacklabelops.crow.application.config;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Global {

	private static Logger LOG = LoggerFactory.getLogger(Global.class);

	private String shellCommand;

	private String workingDirectory;

	private String execution;

	private String errorMode;

	private Map<String, String> environments = new HashMap<>();

	public Global() {
		super();
	}

	public Global(Global anotherGlobal) {
		super();
		try {
			BeanUtils.copyProperties(this, anotherGlobal);
		} catch (IllegalAccessException | InvocationTargetException e) {
			LOG.error("Could not copy another Global.");
		}
		if (anotherGlobal.getEnvironments() != null) {
			this.environments = new HashMap<>(anotherGlobal.getEnvironments());
		} else {
			this.environments = null;
		}
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

	public Map<String, String> getEnvironments() {
		return environments;
	}

	public void setEnvironments(Map<String, String> environments) {
		this.environments = environments;
	}

	@Override
	public String toString() {
		return String.format(
				"Global [shellCommand=%s, workingDirectory=%s, execution=%s, errorMode=%s, environments=%s]",
				shellCommand, workingDirectory, execution, errorMode, environments);
	}

}
