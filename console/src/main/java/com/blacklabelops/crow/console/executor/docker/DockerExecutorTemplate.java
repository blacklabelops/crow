package com.blacklabelops.crow.console.executor.docker;

import java.util.List;

import com.blacklabelops.crow.console.definition.JobDefinition;
import com.blacklabelops.crow.console.executor.IExecutor;
import com.blacklabelops.crow.console.executor.JobExecutorTemplate;
import com.blacklabelops.crow.console.logger.IJobLogger;
import com.blacklabelops.crow.console.logger.IJobLoggerFactory;
import com.blacklabelops.crow.console.reporter.IJobReporter;
import com.blacklabelops.crow.console.reporter.IJobReporterFactory;

public class DockerExecutorTemplate extends JobExecutorTemplate {

	public DockerExecutorTemplate(JobDefinition definition, List<IJobReporterFactory> reporter, List<IJobLoggerFactory> logger) {
		super(definition, reporter, logger);
	}
	
	@Override
	public IExecutor createExecutor() {
		List<IJobReporter> reporters = createReporters();
        List<IJobLogger> logger = createLoggers();
        return new DockerExecutor(getJobDefinition(), reporters, logger);
	}

}
