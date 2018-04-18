package com.blacklabelops.crow.console.executor;

import java.util.List;

import com.blacklabelops.crow.console.definition.JobDefinition;
import com.blacklabelops.crow.console.logger.IJobLogger;
import com.blacklabelops.crow.console.reporter.IJobReporter;

public interface IExecutor extends Runnable {

	public void run();

	public String getJobId();

	public List<IJobReporter> getReporter();

	JobDefinition getJobDefinition();

	ExecutionResult getExecutionResult();

	public void addReporter(List<IJobReporter> reporters);

	public void addLogger(List<IJobLogger> loggers);

	public void deleteReporters();

	public void deleteLoggers();
}
