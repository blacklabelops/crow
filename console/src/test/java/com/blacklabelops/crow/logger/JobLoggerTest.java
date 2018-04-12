package com.blacklabelops.crow.logger;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class JobLoggerTest {

    public JobLogLogger jobLogLogger = new JobLogLogger("jobName");

    @Test
    public void testInitializeLogger_SimplyCalled_NoException() {
        jobLogLogger.initializeLogger();
    }

    @Test
    public void testFinishLogger_SimplyCalled_NoException() {
        jobLogLogger.finishLogger();
    }

    @Test
    public void testInfoLogConsumer_DefaultInfoLogger_NeverNull() {
        assertNotNull(jobLogLogger.getInfoLogConsumer());
    }

    @Test
    public void getErrorLogConsumer_DefaultErrorLogger_NeverNull() {
        assertNotNull(jobLogLogger.getErrorLogConsumer());
    }
}