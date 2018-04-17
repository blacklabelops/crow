package com.blacklabelops.crow.logger;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

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
