package com.blacklabelops.crow.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by steffenbleul on 23.12.16.
 */
public class ExecutorPool {

    public static Logger LOG = LoggerFactory.getLogger(ExecutorPool.class);

    public Map<String, Thread> runningExecutors = new HashMap<>();

    public ExecutorPool() {
        super();
    }

    public ExecutionResult addExecution(IExecutor executor) {
        ExecutionMode executionMode = executor.getExecutionMode();
        ExecutionResult result = null;
        boolean canBeExecuted = ExecutionMode.PARALLEL.equals(executionMode) || !checkRunning(executor);
        if (canBeExecuted) {
            LOG.debug("Starting new instance of  Job {}.", executor.getJobName());
            Thread execution = new Thread(executor);
            runningExecutors.put(executor.getJobName(), execution);
            execution.start();
            result = ExecutionResult.EXECUTED;
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
