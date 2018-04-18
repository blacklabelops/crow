package com.blacklabelops.crow.console.logger;

import org.slf4j.Logger;

import com.blacklabelops.crow.console.executor.BoundedLineWriter;

public class LogInfoConsumer extends BoundedLineWriter {

	private Logger logger;

	public LogInfoConsumer(Logger pLogger) {
		super(true);
		logger = pLogger;
	}

	@Override
	protected void writeLine(String line) {
		logger.info(line);

	}

}
