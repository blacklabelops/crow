package com.blacklabelops.crow.console.logger;

import org.slf4j.Logger;

import com.blacklabelops.crow.console.executor.BoundedLineWriter;

public class LogErrorConsumer extends BoundedLineWriter {

	private Logger logger;

	public LogErrorConsumer(
			Logger pLogger) {
		super(true);
		logger = pLogger;
	}

	@Override
	protected void writeLine(String line) {
		logger.error(line);
	}

}
