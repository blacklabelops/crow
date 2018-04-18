package com.blacklabelops.crow.console.dispatcher;

import java.util.List;

import com.blacklabelops.crow.console.executor.IExecutorTemplate;
import com.blacklabelops.crow.console.logger.IJobLogger;
import com.blacklabelops.crow.console.reporter.IJobReporter;

public interface IDispatcher {

	void addJob(IExecutorTemplate executorTemplate);

	void removeJob(String jobName);

	DispatchingResult execute(String jobName);

	DispatchingResult execute(String jobId, List<IJobReporter> reporters, List<IJobLogger> loggers);

	void testExecute(String jobId, List<IJobReporter> reporters, List<IJobLogger> loggers);

}