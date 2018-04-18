package com.blacklabelops.crow.console.reporter;

import com.blacklabelops.crow.console.executor.ExecutionResult;

public interface IJobReporter {

    default void startingJob(ExecutionResult executingJob) {

    }

    default void finishedJob(ExecutionResult executingJob) {

    }

    default void failingJob(ExecutionResult executingJob) {

    }
}
