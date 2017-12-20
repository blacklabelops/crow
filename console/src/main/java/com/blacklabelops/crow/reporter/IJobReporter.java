package com.blacklabelops.crow.reporter;

import com.blacklabelops.crow.executor.IExecutor;

public interface IJobReporter {

    default void startingJob(IExecutor executingJob) {

    }

    default void finishedJob(IExecutor executingJob) {

    }

    default void failingJob(IExecutor executingJob) {

    }
}
