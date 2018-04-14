package com.blacklabelops.crow.executor;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorPool {

    private static Logger LOG = LoggerFactory.getLogger(ExecutorPool.class);

    private Map<String, WeakReference<Thread>> runningExecutors = Collections.synchronizedMap(new HashMap<>());

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
            runningExecutors.put(executor.getJobName(), new WeakReference<Thread>(execution));
            execution.start();
        } else {
            LOG.debug("Skipping Job {}, already running!", executor.getJobName());
            result = ExecutionResult.DROPPED_ALREADY_RUNNING;
        }
        return result;
    }

    public boolean checkRunning(IExecutor executor) {
        boolean alreadyRunning = true;
    		WeakReference<Thread> reference = runningExecutors.get(executor.getJobName());
    		if (reference != null) {
    			Thread thread = reference.get();
    			if (thread == null || thread.getState() == Thread.State.TERMINATED) {
    				alreadyRunning = false;
             }
    		} else {
    			alreadyRunning = false;
    		}
        return alreadyRunning;
    }
    
    

}
