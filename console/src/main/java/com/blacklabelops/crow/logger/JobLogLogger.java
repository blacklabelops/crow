package com.blacklabelops.crow.logger;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobLogLogger implements IJobLogger {

    private final Logger jobLogger;

    private final LogInfoConsumer loginfoConsumer;

    private final LogErrorConsumer logerrorConsumer;

    public JobLogLogger(String jobName) {
        jobLogger = LoggerFactory.getLogger(jobName);
        loginfoConsumer = new LogInfoConsumer(jobLogger);
        logerrorConsumer = new LogErrorConsumer(jobLogger);
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
