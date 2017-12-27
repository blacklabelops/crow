package com.blacklabelops.crow.logger;

public class JobLoggerFactory implements IJobLoggerFactory {

    private final String jobName;

    public JobLoggerFactory(String jobName) {
        super();
        this.jobName = jobName;
    }

    @Override
    public IJobLogger createInstance() {
        return new JobLogLogger(this.jobName);
    }
}
