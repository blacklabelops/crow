package com.blacklabelops.crow.reporter;

import com.blacklabelops.crow.executor.IExecutor;
import com.blacklabelops.crow.scheduler.IScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionErrorReporter implements IJobReporter {

    private Logger logger = LoggerFactory.getLogger(ConsoleReporter.class);

    private final IScheduler scheduler;

    public ExecutionErrorReporter(IScheduler scheduler) {
        super();
        this.scheduler = scheduler;
    }

    @Override
    public void failingJob(IExecutor executingJob) {
        logger.debug("Notifying a failing job {}!", executingJob.getJobName());
        scheduler.notifyExecutionError(executingJob, executingJob.getReturnCode());
    }
}
