package com.blacklabelops.crow.scheduler;

import com.blacklabelops.crow.executor.IExecutor;
import com.blacklabelops.crow.executor.IExecutorTemplate;
import com.blacklabelops.crow.executor.JobExecutorTemplate;

import java.time.ZonedDateTime;

public class Job {

    private final IExecutorTemplate jobExecutorTemplate;

    private final IExecutionTime jobExcutionTime;

    private ZonedDateTime lastExecution = ZonedDateTime.now();

    public Job(IExecutorTemplate executorTemplate, IExecutionTime executionTime) {
        super();
        jobExecutorTemplate = executorTemplate;
        jobExcutionTime = executionTime;
    }

    public String getJobName() {
        return jobExecutorTemplate.getJobName();
    }

    public ZonedDateTime getNextExecution() {
        return jobExcutionTime.nextExecution(lastExecution);
    }

    public IExecutor getExecutor() {
        return jobExecutorTemplate.createExecutor();
    }

    public void setLastExecution(ZonedDateTime theLastExecution) {
        lastExecution = theLastExecution;
    }
}
