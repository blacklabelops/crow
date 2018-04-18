package com.blacklabelops.crow.console.logger;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.blacklabelops.crow.console.logger.JobLogLogger;

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
		jobLogLogger.initializeLogger();
		assertNotNull(jobLogLogger.getInfoLogConsumer());
	}

	@Test
	public void getErrorLogConsumer_DefaultErrorLogger_NeverNull() {
		jobLogLogger.initializeLogger();
		assertNotNull(jobLogLogger.getErrorLogConsumer());
	}
}