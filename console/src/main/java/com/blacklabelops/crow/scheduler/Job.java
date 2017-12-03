package com.blacklabelops.crow.scheduler;

import com.blacklabelops.crow.executor.IExecutor;

import java.time.ZonedDateTime;

/**
 * Created by steffenbleul on 22.12.16.
 */
public class Job {

    private final IExecutor jobExecutor;

    private final IExecutionTime jobExcutionTime;

    private ZonedDateTime lastExecution = ZonedDateTime.now();

    public Job(IExecutor executor, IExecutionTime executionTime) {
        super();
        jobExecutor = executor;
        jobExcutionTime = executionTime;
    }

    public String getJobName() {
        return jobExecutor.getJobName();
    }

    public ZonedDateTime getNextExecution() {
        return jobExcutionTime.nextExecution(lastExecution);
    }

    public IExecutor getExecutor() {
        return jobExecutor;
    }

    public void setLastExecution(ZonedDateTime theLastExecution) {
        lastExecution = theLastExecution;
    }
}
