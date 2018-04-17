package com.blacklabelops.crow.logger;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobLogLogger implements IJobLogger {

	private final Logger jobLogger;

	private OutputStream loginfoConsumer;

	private OutputStream logerrorConsumer;

	public JobLogLogger(String jobName) {
		jobLogger = LoggerFactory.getLogger(jobName);
	}

	@Override
	public void initializeLogger() {
		loginfoConsumer = new WriterOutputStream(new LogInfoConsumer(jobLogger), StandardCharsets.UTF_8);
		logerrorConsumer = new WriterOutputStream(new LogErrorConsumer(jobLogger), StandardCharsets.UTF_8);
	}

	@Override
	public void finishLogger() {
		IOUtils.closeQuietly(loginfoConsumer);
		IOUtils.closeQuietly(logerrorConsumer);
	}

	@Override
	public OutputStream getInfoLogConsumer() {
		return loginfoConsumer;
	}

	@Override
	public OutputStream getErrorLogConsumer() {
		return logerrorConsumer;
	}
}
