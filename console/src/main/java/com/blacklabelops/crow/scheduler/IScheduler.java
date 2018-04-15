package com.blacklabelops.crow.scheduler;

import com.blacklabelops.crow.dispatcher.DispatchingResult;
import com.blacklabelops.crow.executor.ExecutionResult;

public interface IScheduler {

    void addJob(Job job);

    Job getNextExecutableJob();

    void notifyDispatchingError(DispatchingResult dispatcherResult);

    void notifyExecutionError(ExecutionResult executionResult);

	void removeJob(Job job);

}
