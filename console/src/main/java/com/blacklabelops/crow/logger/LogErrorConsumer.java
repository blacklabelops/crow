package com.blacklabelops.crow.logger;

import org.slf4j.Logger;

import com.blacklabelops.crow.executor.BoundedLineWriter;

public class LogErrorConsumer extends BoundedLineWriter {

	private Logger logger;

	public LogErrorConsumer(
			Logger pLogger) {
		super();
		logger = pLogger;
	}

	@Override
	protected void writeLine(String line) {
		logger.error(line);
	}

}
