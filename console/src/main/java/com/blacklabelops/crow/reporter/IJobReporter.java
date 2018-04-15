package com.blacklabelops.crow.reporter;

import com.blacklabelops.crow.executor.ExecutionResult;

public interface IJobReporter {

    default void startingJob(ExecutionResult executingJob) {

    }

    default void finishedJob(ExecutionResult executingJob) {

    }

    default void failingJob(ExecutionResult executingJob) {

    }
}
