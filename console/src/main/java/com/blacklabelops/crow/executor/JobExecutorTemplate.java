package com.blacklabelops.crow.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.logger.IJobLogger;
import com.blacklabelops.crow.logger.IJobLoggerFactory;
import com.blacklabelops.crow.reporter.IJobReporter;
import com.blacklabelops.crow.reporter.IJobReporterFactory;

public abstract class JobExecutorTemplate implements IExecutorTemplate {

	private final String jobId;

	private final JobDefinition jobDefinition;

	private final List<IJobReporterFactory> jobReporterFactories = new ArrayList<>();

	private final List<IJobLoggerFactory> jobLoggerFactories = new ArrayList<>();

	public JobExecutorTemplate(JobDefinition definition, List<IJobReporterFactory> reporter,
			List<IJobLoggerFactory> logger) {
		super();
		jobId = definition.resolveJobId();
		jobDefinition = definition;
		if (reporter != null) {
			reporter
					.stream()
					.filter(Objects::nonNull)
					.forEach(report -> jobReporterFactories.add(report));
		}
		if (logger != null) {
			logger
					.stream()
					.filter(Objects::nonNull)
					.forEach(log -> jobLoggerFactories.add(log));
		}
	}

	@Override
	public abstract IExecutor createExecutor();

	protected List<IJobLogger> createLoggers() {
		List<IJobLogger> loggers = new ArrayList<>(jobLoggerFactories.size());
		jobLoggerFactories.stream().forEach(loggerFactory -> loggers.add(loggerFactory.createInstance()));
		return loggers;
	}

	protected List<IJobReporter> createReporters() {
		List<IJobReporter> reporters = new ArrayList<>(jobReporterFactories.size());
		jobReporterFactories.stream().forEach(reporterFactory -> reporters.add(reporterFactory.createInstance()));
		return reporters;
	}

	@Override
	public String getJobId() {
		return jobId;
	}

	protected JobDefinition getJobDefinition() {
		return new JobDefinition(jobDefinition);
	}

}
