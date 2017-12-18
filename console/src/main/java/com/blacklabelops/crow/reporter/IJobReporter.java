package com.blacklabelops.crow.reporter;

import com.blacklabelops.crow.executor.IExecutor;

public interface IJobReporter {

    default void startingJob(IExecutor pExecutingJob) {

    }

    default void finishedJob(IExecutor pExecutingJob) {

    }
}
