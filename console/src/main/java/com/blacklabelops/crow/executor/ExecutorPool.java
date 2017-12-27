package com.blacklabelops.crow.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ExecutorPool {

    private static Logger LOG = LoggerFactory.getLogger(ExecutorPool.class);

    private Map<String, Thread> runningExecutors = new HashMap<>();

    public ExecutorPool() {
        super();
    }

    public ExecutionResult addExecution(IExecutor executor) {
        ExecutionMode executionMode = executor.getExecutionMode();
        ExecutionResult result = ExecutionResult.EXECUTED;
        boolean canBeExecuted = ExecutionMode.PARALLEL.equals(executionMode) || !checkRunning(executor);
        if (canBeExecuted) {
            LOG.debug("Starting new instance of  Job {}.", executor.getJobName());
            Thread execution = new Thread(executor);
            runningExecutors.put(executor.getJobName(), execution);
            execution.start();
        } else {
            LOG.debug("Skipping Job {}, already running!", executor.getJobName());
            result = ExecutionResult.DROPPED_ALREADY_RUNNING;
        }
        return result;
    }

    public boolean checkRunning(IExecutor executor) {
        boolean alreadyRunning = true;
        if (runningExecutors.containsKey(executor.getJobName())) {
            if (runningExecutors.get(executor.getJobName()).getState() == Thread.State.TERMINATED) {
                alreadyRunning = false;
            }
        } else {
            alreadyRunning = false;
        }
        return alreadyRunning;
    }

}
