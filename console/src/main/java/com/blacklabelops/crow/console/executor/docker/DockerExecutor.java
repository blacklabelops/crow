package com.blacklabelops.crow.console.executor.docker;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.bouncycastle.util.io.TeeOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.console.definition.JobDefinition;
import com.blacklabelops.crow.console.executor.ExecutionResult;
import com.blacklabelops.crow.console.executor.IExecutor;
import com.blacklabelops.crow.console.logger.IJobLogger;
import com.blacklabelops.crow.console.reporter.IJobReporter;

class DockerExecutor implements IExecutor {

	public static final Logger LOG = LoggerFactory.getLogger(DockerExecutor.class);

	private final static int RETURN_CODE_OKAY = 0;

	private final String jobName;

	private final String jobId;

	private final JobDefinition jobDefinition;

	private final List<IJobReporter> jobReporter = new ArrayList<>();

	private final List<IJobLogger> jobLogger = new ArrayList<>();

	private ExecutionResult executionResult;

	public DockerExecutor(JobDefinition definition, List<IJobReporter> reporter, List<IJobLogger> logger) {
		super();
		jobId = definition.resolveJobId();
		jobName = definition.getJobName();
		jobDefinition = new JobDefinition(definition);
		this.executionResult = new ExecutionResult(jobDefinition);
		if (reporter != null) {
			reporter.stream().filter(Objects::nonNull).forEach(report -> jobReporter.add(report));
		}
		if (logger != null) {
			logger.stream().filter(Objects::nonNull).forEach(log -> jobLogger.add(log));
		}
	}

	@Override
	public void run() {
		try {
			jobLogger.forEach(IJobLogger::initializeLogger);
			RemoteContainer executor = new RemoteContainer();
			initializeStreams(executor);
			this.executionResult.setStartingTime(LocalDateTime.now());
			jobReporter.parallelStream()
					.forEach(reporter -> reporter.startingJob(new ExecutionResult(this.executionResult)));
			executor.execute(jobDefinition);
			this.executionResult.setTimedOut(executor.isTimedOut());
			this.executionResult.setReturnCode(executor.getReturnCode());
			this.executionResult.setFinishingTime(LocalDateTime.now());
			jobReporter.parallelStream()
					.forEach(reporter -> reporter.finishedJob(new ExecutionResult(this.executionResult)));
			if (!this.executionResult.isTimedOut()) {
				if (this.executionResult.getReturnCode() == null || RETURN_CODE_OKAY != this.executionResult
						.getReturnCode()) {
					jobReporter.parallelStream()
							.forEach(reporter -> reporter.failingJob(new ExecutionResult(this.executionResult)));
				}
			} else {
				jobReporter.parallelStream()
						.forEach(reporter -> reporter.failingJob(new ExecutionResult(this.executionResult)));
			}
		} catch (Exception e) {
			LOG.error("Execution of Job {} failed!", this.jobDefinition.resolveJobLabel(), e);
			jobReporter.parallelStream()
					.forEach(reporter -> reporter.failingJob(new ExecutionResult(this.executionResult)));
		} finally {
			jobLogger.forEach(IJobLogger::finishLogger);
		}
	}

	private void initializeStreams(RemoteContainer executor) {
		if (jobLogger.size() > 1) {
			executor.setOutStream(recursiveInfoStream(0, jobLogger));
			executor.setOutErrorStream(recursiveErrorStream(0, jobLogger));
		} else {
			Optional<IJobLogger> logger = jobLogger.stream().findFirst();
			logger.ifPresent(l -> executor.setOutStream(l.getInfoLogConsumer()));
			logger.ifPresent(l -> executor.setOutErrorStream(l.getErrorLogConsumer()));
		}
	}

	private OutputStream recursiveInfoStream(int i, List<IJobLogger> jobLoggers) {
		if (i < jobLoggers.size()) {
			int recursion = i + 1;
			OutputStream out = recursiveInfoStream(recursion, jobLoggers);
			if (out == null) {
				return jobLoggers.get(i).getInfoLogConsumer();
			} else {
				return new TeeOutputStream(out, jobLoggers.get(i).getInfoLogConsumer());
			}
		} else {
			return null;
		}
	}

	private OutputStream recursiveErrorStream(int i, List<IJobLogger> jobLoggers) {
		if (i < jobLoggers.size()) {
			int recursion = i + 1;
			OutputStream out = recursiveErrorStream(recursion, jobLoggers);
			if (out == null) {
				return jobLogger.get(i).getErrorLogConsumer();
			} else {
				return new TeeOutputStream(out, jobLogger.get(i).getErrorLogConsumer());
			}
		} else {
			return null;
		}
	}

	public String getJobName() {
		return jobName;
	}

	@Override
	public List<IJobReporter> getReporter() {
		List<IJobReporter> copy = new ArrayList<>(this.jobReporter.size());
		Collections.copy(jobReporter, copy);
		return copy;
	}

	@Override
	public JobDefinition getJobDefinition() {
		return new JobDefinition(jobDefinition);
	}

	@Override
	public ExecutionResult getExecutionResult() {
		return new ExecutionResult(this.executionResult);
	}

	@Override
	public String getJobId() {
		return jobId;
	}

	@Override
	public void addReporter(List<IJobReporter> reporters) {
		this.jobReporter.addAll(reporters);
	}

	@Override
	public void addLogger(List<IJobLogger> loggers) {
		this.jobLogger.addAll(loggers);
	}

	@Override
	public void deleteReporters() {
		this.jobReporter.clear();
	}

	@Override
	public void deleteLoggers() {
		this.jobLogger.clear();
	}

}