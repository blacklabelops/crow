package com.blacklabelops.crow.dispatcher;

import java.util.List;

import com.blacklabelops.crow.executor.IExecutorTemplate;
import com.blacklabelops.crow.logger.IJobLogger;
import com.blacklabelops.crow.reporter.IJobReporter;

public interface IDispatcher {

	void addJob(IExecutorTemplate executorTemplate);

	void removeJob(String jobName);

	DispatchingResult execute(String jobName);

	DispatchingResult execute(String jobId, List<IJobReporter> reporters, List<IJobLogger> loggers);

	void testExecute(String jobId, List<IJobReporter> reporters, List<IJobLogger> loggers);

}