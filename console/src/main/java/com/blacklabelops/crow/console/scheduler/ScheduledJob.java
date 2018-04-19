package com.blacklabelops.crow.console.scheduler;

import java.time.ZonedDateTime;

import com.blacklabelops.crow.console.definition.JobId;

public class ScheduledJob {

	private final IExecutionTime jobExcutionTime;

	private ZonedDateTime lastExecution = ZonedDateTime.now();

	private final JobId jobId;

	public ScheduledJob(JobId jobId, IExecutionTime executionTime) {
		super();
		this.jobId = jobId;
		jobExcutionTime = executionTime;
	}

	public JobId getJobId() {
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
