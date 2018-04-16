package com.blacklabelops.crow.reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.executor.ExecutionResult;
import com.blacklabelops.crow.scheduler.IScheduler;

public class ExecutionErrorReporter implements IJobReporter {

    private Logger logger = LoggerFactory.getLogger(ConsoleReporter.class);

    private final IScheduler scheduler;

    public ExecutionErrorReporter(IScheduler scheduler) {
        super();
        this.scheduler = scheduler;
    }

    @Override
    public void failingJob(ExecutionResult executingJob) {
        logger.debug("Notifying a failing job {}!", executingJob.getJobName());
        scheduler.notifyExecutionError(executingJob);
    }
}
