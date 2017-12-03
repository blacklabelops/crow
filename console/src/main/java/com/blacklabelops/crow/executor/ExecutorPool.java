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

    public void addExecution(IExecutor executor) {
        boolean alreadyRunning = checkRunning(executor);
        if (!alreadyRunning) {
            LOG.debug("Starting new instance of  Job {}.", executor.getJobName());
            Thread execution = new Thread(executor);
            runningExecutors.put(executor.getJobName(), execution);
            execution.start();
        } else {
            LOG.debug("Skipping Job {}, already running!", executor.getJobName());
        }
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
