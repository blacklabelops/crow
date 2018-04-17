package com.blacklabelops.crow.executor.docker;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.executor.ExecutionResult;
import com.blacklabelops.crow.executor.IExecutor;
import com.blacklabelops.crow.logger.IJobLogger;
import com.blacklabelops.crow.reporter.IJobReporter;

class DockerExecutor implements IExecutor {

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
		jobLogger.forEach(IJobLogger::initializeLogger);
		try {
			RemoteContainer executor = new RemoteContainer();
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
				if (RETURN_CODE_OKAY != this.executionResult.getReturnCode()) {
					jobReporter.parallelStream()
							.forEach(reporter -> reporter.failingJob(new ExecutionResult(this.executionResult)));
				}
			} else {
				jobReporter.parallelStream()
						.forEach(reporter -> reporter.failingJob(new ExecutionResult(this.executionResult)));
			}
		} finally {
			jobLogger.forEach(IJobLogger::finishLogger);
		}
	}

	public String getJobName() {
		return jobName;
	}

	@Override
	public List<IJobReporter> getReporter() {
		List<IJobReporter> copy = new ArrayList<>();
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

}
