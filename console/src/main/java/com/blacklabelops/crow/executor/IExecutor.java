package com.blacklabelops.crow.executor;

import java.util.List;

import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.reporter.IJobReporter;

public interface IExecutor extends Runnable {

	public void run();

	public String getJobId();

	public List<IJobReporter> getReporter();

	JobDefinition getJobDefinition();

	ExecutionResult getExecutionResult();
}
