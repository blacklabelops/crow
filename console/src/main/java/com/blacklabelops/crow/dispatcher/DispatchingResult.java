package com.blacklabelops.crow.dispatcher;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.executor.ExecutionResult;

public class DispatchingResult {
	
	private static Logger LOG = LoggerFactory.getLogger(DispatchingResult.class);
	
	private JobDefinition jobDefinition;
	
	private String jobName;
	
	private DispatcherResult dispatcherResult;
	
	private LocalDateTime dispatchingTime;
	
	public DispatchingResult() {
		super();
	}
	
	public DispatchingResult(JobDefinition jobDefinition) {
		super();
		this.setJobDefinition(jobDefinition);
		this.setJobName(jobDefinition.getJobName());
		this.setDispatchingTime(LocalDateTime.now());
	}
	
	public DispatchingResult(ExecutionResult executionResult) {
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

	public DispatcherResult getDispatcherResult() {
		return dispatcherResult;
	}

	public void setDispatcherResult(DispatcherResult dispatcherResult) {
		this.dispatcherResult = dispatcherResult;
	}

	public LocalDateTime getDispatchingTime() {
		return dispatchingTime;
	}

	public void setDispatchingTime(LocalDateTime dispatchingTime) {
		this.dispatchingTime = dispatchingTime;
	}

	@Override
	public String toString() {
		return String.format(
				"DispatchingResult [jobDefinition=%s, jobName=%s, dispatcherResult=%s, dispatchingTime=%s]",
				jobDefinition, jobName, dispatcherResult, dispatchingTime);
	}
	
}
