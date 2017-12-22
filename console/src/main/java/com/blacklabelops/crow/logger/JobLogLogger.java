package com.blacklabelops.crow.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.function.Consumer;

public class JobLogLogger implements IJobLogger {

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
    }

    @Override
    public void finishLogger() {
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
