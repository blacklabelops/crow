package com.blacklabelops.crow.console.executor;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.definition.JobId;

public class ExecutionResult {

	private static Logger LOG = LoggerFactory.getLogger(ExecutionResult.class);

	private Job jobDefinition;

	private JobId jobId;

	private Integer returnCode;

	private boolean timedOut;

	private LocalDateTime startingTime;

	private LocalDateTime finishingTime;

	private LocalDateTime errorTime;

	public ExecutionResult() {
		super();
	}

	public ExecutionResult(Job jobDefinition) {
		super();
		this.jobDefinition = jobDefinition;
		this.jobId = jobDefinition.getId();
	}

	public ExecutionResult(ExecutionResult executionResult) {
		super();
		try {
			BeanUtils.copyProperties(this, executionResult);
		} catch (IllegalAccessException | InvocationTargetException e) {
			LOG.error("ExecutionResult konnte nicht geclont werden!");
		}
		this.jobDefinition = executionResult.getJobDefinition();
	}

	public Job getJobDefinition() {
		return jobDefinition;
	}

	public Integer getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(Integer returnCode) {
		this.returnCode = returnCode;
	}

	public boolean isTimedOut() {
		return timedOut;
	}

	public void setTimedOut(boolean timedOut) {
		this.timedOut = timedOut;
	}

	public LocalDateTime getStartingTime() {
		return startingTime;
	}

	public void setStartingTime(LocalDateTime startingTime) {
		this.startingTime = startingTime;
	}

	public LocalDateTime getFinishingTime() {
		return finishingTime;
	}

	public void setFinishingTime(LocalDateTime finishingTime) {
		this.finishingTime = finishingTime;
	}

	public LocalDateTime getErrorTime() {
		return errorTime;
	}

	public void setErrorTime(LocalDateTime errorTime) {
		this.errorTime = errorTime;
	}

	public JobId getJobId() {
		return jobId;
	}

}
