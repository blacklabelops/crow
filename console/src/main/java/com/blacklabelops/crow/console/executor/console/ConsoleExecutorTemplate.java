package com.blacklabelops.crow.console.executor.console;

import java.util.List;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.executor.IExecutor;
import com.blacklabelops.crow.console.executor.JobExecutorTemplate;
import com.blacklabelops.crow.console.logger.IJobLogger;
import com.blacklabelops.crow.console.logger.IJobLoggerFactory;
import com.blacklabelops.crow.console.reporter.IJobReporter;
import com.blacklabelops.crow.console.reporter.IJobReporterFactory;

public class ConsoleExecutorTemplate extends JobExecutorTemplate {

	public ConsoleExecutorTemplate(Job definition, List<IJobReporterFactory> reporter, List<IJobLoggerFactory> logger) {
		super(definition, reporter, logger);
	}

	@Override
	public IExecutor createExecutor() {
		List<IJobReporter> reporters = createReporters();
		List<IJobLogger> logger = createLoggers();
		return new ConsoleExecutor(getJobDefinition(), reporters, logger);
	}

}
