package com.blacklabelops.crow.console.scheduler;

import com.blacklabelops.crow.console.dispatcher.AbstractDispatchingResult;
import com.blacklabelops.crow.console.executor.ExecutionResult;

public interface IScheduler {

    void addJob(ScheduledJob job);

    ScheduledJob getNextExecutableJob();

    void notifyDispatchingError(AbstractDispatchingResult dispatcherResult);

    void notifyExecutionError(ExecutionResult executionResult);

	void removeJob(ScheduledJob job);

}
