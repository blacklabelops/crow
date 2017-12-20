package com.blacklabelops.crow.scheduler;

import com.blacklabelops.crow.executor.ExecutionResult;
import com.blacklabelops.crow.executor.IExecutor;

/**
 * Created by steffenbleul on 23.12.16.
 */
public interface IScheduler {

    void addJob(Job job);

    Job getNextExecutableJob();

    void notifyFailingJob(IExecutor executor, ExecutionResult result);

    void notifyExecutionError(IExecutor executor, Integer returnCode);

}
