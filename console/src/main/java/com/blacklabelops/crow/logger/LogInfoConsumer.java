package com.blacklabelops.crow.logger;

import org.slf4j.Logger;

import com.blacklabelops.crow.executor.BoundedLineWriter;

public class LogInfoConsumer extends BoundedLineWriter {

	private Logger logger;

	public LogInfoConsumer(Logger pLogger) {
		super();
		logger = pLogger;
	}

	@Override
	protected void writeLine(String line) {
		logger.info(line);

	}

}
