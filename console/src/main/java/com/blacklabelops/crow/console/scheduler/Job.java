package com.blacklabelops.crow.console.scheduler;

import java.time.ZonedDateTime;

public class Job {

	private final IExecutionTime jobExcutionTime;

	private ZonedDateTime lastExecution = ZonedDateTime.now();

	private final String jobId;

	public Job(String jobId, IExecutionTime executionTime) {
		super();
		this.jobId = jobId;
		jobExcutionTime = executionTime;
	}

	public String getJobId() {
		return jobId;
	}

	public ZonedDateTime getNextExecution() {
		return jobExcutionTime.nextExecution(lastExecution);
	}

	public void setLastExecution(ZonedDateTime theLastExecution) {
		lastExecution = theLastExecution;
	}

	@Override
	public String toString() {
		return String.format("Job [jobExcutionTime=%s, lastExecution=%s, jobId=%s]", jobExcutionTime, lastExecution,
				jobId);
	}

}