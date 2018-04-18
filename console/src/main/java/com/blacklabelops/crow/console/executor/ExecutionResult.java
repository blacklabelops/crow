package com.blacklabelops.crow.console.executor;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.console.definition.JobDefinition;

public class ExecutionResult {

	private static Logger LOG = LoggerFactory.getLogger(ExecutionResult.class);

	private JobDefinition jobDefinition;

	private String jobName;

	private String jobId;

	private Integer returnCode;

	private boolean timedOut;

	private LocalDateTime startingTime;

	private LocalDateTime finishingTime;

	private LocalDateTime errorTime;

	public ExecutionResult() {
		super();
	}

	public ExecutionResult(JobDefinition jobDefinition) {
		super();
		this.setJobDefinition(jobDefinition);
		this.setJobName(jobDefinition.getJobName());
		this.setJobId(jobDefinition.resolveJobId());
	}

	public ExecutionResult(ExecutionResult executionResult) {
		super();
		try {
			BeanUtils.copyProperties(this, executionResult);
		} catch (IllegalAccessException | InvocationTargetException e) {
			LOG.error("ExecutionResult konnte nicht geclont werden!");
		}
		this.setJobDefinition(executionResult.getJobDefinition());
	}

	public JobDefinition getJobDefinition() {
		if (jobDefinition != null) {
			return new JobDefinition(jobDefinition);
		} else {
			return jobDefinition;
		}
	}

	public void setJobDefinition(JobDefinition jobDefinition) {
		if (jobDefinition != null) {
			this.jobDefinition = new JobDefinition(jobDefinition);
		} else {
			this.jobDefinition = jobDefinition;
		}
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
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

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

}
