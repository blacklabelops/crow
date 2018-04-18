package com.blacklabelops.crow.executor.docker;

import java.util.List;

import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.executor.IExecutor;
import com.blacklabelops.crow.executor.JobExecutorTemplate;
import com.blacklabelops.crow.logger.IJobLogger;
import com.blacklabelops.crow.logger.IJobLoggerFactory;
import com.blacklabelops.crow.reporter.IJobReporter;
import com.blacklabelops.crow.reporter.IJobReporterFactory;

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
