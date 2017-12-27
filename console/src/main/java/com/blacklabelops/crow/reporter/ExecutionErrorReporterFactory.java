package com.blacklabelops.crow.reporter;

import com.blacklabelops.crow.scheduler.IScheduler;

public class ExecutionErrorReporterFactory implements IJobReporterFactory {

    private final IScheduler scheduler;

    public ExecutionErrorReporterFactory(IScheduler scheduler) {
        super();
        this.scheduler = scheduler;
    }

    @Override
    public IJobReporter createInstance() {
        return new ExecutionErrorReporter(scheduler);
    }
}
