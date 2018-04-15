package com.blacklabelops.crow.scheduler;

import java.time.ZonedDateTime;

public class Job {

    private final IExecutionTime jobExcutionTime;

    private ZonedDateTime lastExecution = ZonedDateTime.now();

	private final String jobName;

    public Job(String jobName, IExecutionTime executionTime) {
        super();
        this.jobName = jobName;
        jobExcutionTime = executionTime;
    }

    public String getJobName() {
        return jobName;
    }

    public ZonedDateTime getNextExecution() {
        return jobExcutionTime.nextExecution(lastExecution);
    }

    public void setLastExecution(ZonedDateTime theLastExecution) {
        lastExecution = theLastExecution;
    }
}
