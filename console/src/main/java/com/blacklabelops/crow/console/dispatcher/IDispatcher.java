package com.blacklabelops.crow.console.dispatcher;

import java.util.List;

import com.blacklabelops.crow.console.definition.JobId;
import com.blacklabelops.crow.console.executor.IExecutorTemplate;
import com.blacklabelops.crow.console.logger.IJobLogger;
import com.blacklabelops.crow.console.reporter.IJobReporter;

public interface IDispatcher {

	void addJob(IExecutorTemplate executorTemplate);

	void removeJob(JobId jobName);

	AbstractDispatchingResult execute(JobId jobName);

	AbstractDispatchingResult execute(JobId jobId, List<IJobReporter> reporters, List<IJobLogger> loggers);

	void testExecute(JobId jobId, List<IJobReporter> reporters, List<IJobLogger> loggers);

}