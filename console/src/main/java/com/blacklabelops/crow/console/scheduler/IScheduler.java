package com.blacklabelops.crow.console.scheduler;

import com.blacklabelops.crow.console.dispatcher.DispatchingResult;
import com.blacklabelops.crow.console.executor.ExecutionResult;

public interface IScheduler {

    void addJob(Job job);

    Job getNextExecutableJob();

    void notifyDispatchingError(DispatchingResult dispatcherResult);

    void notifyExecutionError(ExecutionResult executionResult);

	void removeJob(Job job);

}
