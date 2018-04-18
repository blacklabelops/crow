package com.blacklabelops.crow.dispatcher;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.executor.IExecutorTemplate;
import com.blacklabelops.crow.executor.console.ConsoleExecutorTemplate;
import com.blacklabelops.crow.executor.docker.DockerExecutorTemplate;
import com.blacklabelops.crow.logger.IJobLogger;
import com.blacklabelops.crow.logger.IJobLoggerFactory;
import com.blacklabelops.crow.logger.JobLoggerFactory;
import com.blacklabelops.crow.reporter.ConsoleReporterFactory;
import com.blacklabelops.crow.reporter.IJobReporter;
import com.blacklabelops.crow.reporter.IJobReporterFactory;
import com.blacklabelops.crow.repository.JobRepository;
import com.cronutils.utils.StringUtils;

@Component
public class JobDispatcher {

	private static Logger LOG = LoggerFactory.getLogger(JobRepository.class);

	public IDispatcher dispatcher = new Dispatcher();

	public JobDispatcher() {
		super();
	}

	public String addJob(JobDefinition jobDefinition, List<IJobReporterFactory> reporters,
			List<IJobLoggerFactory> loggers) {
		String jobId = jobDefinition.resolveJobId();
		List<IJobReporterFactory> reporter = initializeReporters(jobDefinition, reporters);
		List<IJobLoggerFactory> logger = initializeLoggers(jobDefinition, loggers);
		IExecutorTemplate jobTemplate = initializeTemplate(jobDefinition, reporter, logger);
		this.dispatcher.addJob(jobTemplate);
		return jobId;
	}

	private IExecutorTemplate initializeTemplate(JobDefinition jobDefinition, List<IJobReporterFactory> reporter,
			List<IJobLoggerFactory> logger) {
		IExecutorTemplate jobTemplate = null;
		if (isDockerJob(jobDefinition)) {
			jobTemplate = new DockerExecutorTemplate(jobDefinition, reporter, logger);
		} else {
			jobTemplate = new ConsoleExecutorTemplate(jobDefinition, reporter, logger);
		}
		return jobTemplate;
	}

	private List<IJobReporterFactory> initializeReporters(JobDefinition jobDefinition,
			List<IJobReporterFactory> reporters) {
		List<IJobReporterFactory> reporter = new ArrayList<>();
		if (reporters != null) {
			reporter.addAll(reporters);
		}
		reporter.add(new ConsoleReporterFactory());
		return reporter;
	}

	private List<IJobLoggerFactory> initializeLoggers(JobDefinition jobDefinition, List<IJobLoggerFactory> loggers) {
		List<IJobLoggerFactory> logger = new ArrayList<>();
		if (loggers != null) {
			logger.addAll(loggers);
		}
		logger.add(new JobLoggerFactory(getLoggerLabel(jobDefinition)));
		return logger;
	}

	private String getLoggerLabel(JobDefinition jobDefinition) {
		String loggerLabel = jobDefinition.getJobName();
		if (!StringUtils.isEmpty(jobDefinition.getContainerName())) {
			loggerLabel = loggerLabel.concat(" - ").concat(jobDefinition.getContainerName());
		} else if (!StringUtils.isEmpty(jobDefinition.getContainerId())) {
			loggerLabel = loggerLabel.concat(" - ").concat(jobDefinition.getContainerId());
		}
		return loggerLabel;
	}

	private boolean isDockerJob(JobDefinition addedJobDefinition) {
		if (!StringUtils.isEmpty(addedJobDefinition.getContainerName()) || !StringUtils.isEmpty(addedJobDefinition
				.getContainerId())) {
			return true;
		}
		return false;
	}

	public void removeJob(String jobId) {
		this.dispatcher.removeJob(jobId);
	}

	public void execute(String jobId) {
		this.dispatcher.execute(jobId);
	}

	public void execute(String jobId, List<IJobReporter> reporters, List<IJobLogger> loggers) {
		this.dispatcher.execute(jobId, reporters, loggers);
	}

	public void testExecute(String jobId, List<IJobReporter> reporters, List<IJobLogger> loggers) {
		this.dispatcher.testExecute(jobId, reporters, loggers);
	}

	public IDispatcher getDispatcher() {
		return dispatcher;
	}

}
