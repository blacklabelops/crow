package com.blacklabelops.crow.logger;

import com.blacklabelops.crow.suite.FastTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

@Category(FastTests.class)
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