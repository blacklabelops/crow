package com.blacklabelops.crow.console.logger;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.blacklabelops.crow.console.logger.IJobLogger;

public class TestableJobLogger implements IJobLogger {

	ByteArrayOutputStream output;

	ByteArrayOutputStream outputError;

	public TestableJobLogger() {
		super();
	}

	@Override
	public void initializeLogger() {
		output = new ByteArrayOutputStream();
		outputError = new ByteArrayOutputStream();
	}

	@Override
	public void finishLogger() {
	}

	@Override
	public OutputStream getInfoLogConsumer() {
		return output;
	}

	@Override
	public OutputStream getErrorLogConsumer() {
		return outputError;
	}

	public ByteArrayOutputStream getOutput() {
		return output;
	}

	public ByteArrayOutputStream getOutputError() {
		return outputError;
	}

}
