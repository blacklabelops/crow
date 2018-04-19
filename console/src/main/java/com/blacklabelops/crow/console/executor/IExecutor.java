package com.blacklabelops.crow.console.executor;

import java.util.List;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.definition.JobId;
import com.blacklabelops.crow.console.logger.IJobLogger;
import com.blacklabelops.crow.console.reporter.IJobReporter;

public interface IExecutor extends Runnable {

	public void run();

	public JobId getJobId();

	public List<IJobReporter> getReporter();

	Job getJobDefinition();

	ExecutionResult getExecutionResult();

	public void addReporter(List<IJobReporter> reporters);

	public void addLogger(List<IJobLogger> loggers);

	public void deleteReporters();

	public void deleteLoggers();
}
