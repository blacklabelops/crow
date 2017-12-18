package com.blacklabelops.crow.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.function.Consumer;

public class JobLogLogger implements IJobLogger {

    public static final String MDC_JOBNAME = "jobname";

    private final Logger jobLogger;

    private final LogInfoConsumer loginfoConsumer;

    private final LogErrorConsumer logerrorConsumer;

    private final String jobName;

    public JobLogLogger(String jobName) {
        jobLogger = LoggerFactory.getLogger(jobName);
        loginfoConsumer = new LogInfoConsumer(jobLogger);
        logerrorConsumer = new LogErrorConsumer(jobLogger);
        this.jobName = jobName;
    }

    @Override
    public void initializeLogger() {
        MDC.put(MDC_JOBNAME, jobName);
    }

    @Override
    public void finishLogger() {
        MDC.remove(MDC_JOBNAME);
    }

    @Override
    public Consumer<String> getInfoLogConsumer() {
        return loginfoConsumer;
    }

    @Override
    public Consumer<String> getErrorLogConsumer() {
        return logerrorConsumer;
    }
}
