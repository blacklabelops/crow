package com.blacklabelops.crow.application.dispatcher;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.definition.JobId;
import com.blacklabelops.crow.console.dispatcher.Dispatcher;
import com.blacklabelops.crow.console.dispatcher.IDispatcher;
import com.blacklabelops.crow.console.executor.IExecutorTemplate;
import com.blacklabelops.crow.console.executor.console.ConsoleExecutorTemplate;
import com.blacklabelops.crow.console.executor.docker.DockerExecutorTemplate;
import com.blacklabelops.crow.console.logger.IJobLogger;
import com.blacklabelops.crow.console.logger.IJobLoggerFactory;
import com.blacklabelops.crow.console.logger.JobLoggerFactory;
import com.blacklabelops.crow.console.reporter.ConsoleReporterFactory;
import com.blacklabelops.crow.console.reporter.IJobReporter;
import com.blacklabelops.crow.console.reporter.IJobReporterFactory;

@Component
public class JobDispatcher {

	public IDispatcher dispatcher = new Dispatcher();

	public JobDispatcher() {
		super();
	}

	public JobId addJob(Job jobDefinition, List<IJobReporterFactory> reporters,
			List<IJobLoggerFactory> loggers) {
		JobId jobId = jobDefinition.getId();
		List<IJobReporterFactory> reporter = initializeReporters(jobDefinition, reporters);
		List<IJobLoggerFactory> logger = initializeLoggers(jobDefinition, loggers);
		IExecutorTemplate jobTemplate = initializeTemplate(jobDefinition, reporter, logger);
		this.dispatcher.addJob(jobTemplate);
		return jobId;
	}

	private IExecutorTemplate initializeTemplate(Job jobDefinition, List<IJobReporterFactory> reporter,
			List<IJobLoggerFactory> logger) {
		IExecutorTemplate jobTemplate = null;
		if (isDockerJob(jobDefinition)) {
			jobTemplate = new DockerExecutorTemplate(jobDefinition, reporter, logger);
		} else {
			jobTemplate = new ConsoleExecutorTemplate(jobDefinition, reporter, logger);
		}
		return jobTemplate;
	}

	private List<IJobReporterFactory> initializeReporters(Job jobDefinition,
			List<IJobReporterFactory> reporters) {
		List<IJobReporterFactory> reporter = new ArrayList<>();
		if (reporters != null) {
			reporter.addAll(reporters);
		}
		reporter.add(new ConsoleReporterFactory());
		return reporter;
	}

	private List<IJobLoggerFactory> initializeLoggers(Job jobDefinition, List<IJobLoggerFactory> loggers) {
		List<IJobLoggerFactory> logger = new ArrayList<>();
		if (loggers != null) {
			logger.addAll(loggers);
		}
		logger.add(new JobLoggerFactory(jobDefinition.jobLabel()));
		return logger;
	}

	private boolean isDockerJob(Job addedJobDefinition) {
		if (addedJobDefinition.getContainerName().isPresent() || addedJobDefinition.getContainerId().isPresent()) {
			return true;
		}
		return false;
	}

	public void removeJob(JobId jobId) {
		this.dispatcher.removeJob(jobId);
	}

	public void execute(JobId jobId) {
		this.dispatcher.execute(jobId);
	}

	public void execute(JobId jobId, List<IJobReporter> reporters, List<IJobLogger> loggers) {
		this.dispatcher.execute(jobId, reporters, loggers);
	}

	public void testExecute(JobId jobId, List<IJobReporter> reporters, List<IJobLogger> loggers) {
		this.dispatcher.testExecute(jobId, reporters, loggers);
	}

	public IDispatcher getDispatcher() {
		return dispatcher;
	}

}
