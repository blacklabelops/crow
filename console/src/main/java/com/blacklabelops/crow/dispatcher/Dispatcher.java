package com.blacklabelops.crow.dispatcher;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.definition.ExecutionMode;
import com.blacklabelops.crow.executor.IExecutor;
import com.blacklabelops.crow.executor.IExecutorTemplate;

public class Dispatcher implements IDispatcher {

    private static Logger LOG = LoggerFactory.getLogger(Dispatcher.class);
    
    private Map<String, IExecutorTemplate> jobs = Collections.synchronizedMap(new HashMap<>());
    
    private Map<String, WeakReference<Thread>> runningExecutors = Collections.synchronizedMap(new HashMap<>());

    public Dispatcher() {
        super();
    }
    
    @Override
    public void addJob(IExecutorTemplate executorTemplate) {
    		jobs.put(executorTemplate.getJobName(), executorTemplate);
    }
    
    @Override
    public void removeJob(String jobName) {
    		jobs.remove(jobName);
    }
    
    @Override
    public DispatchingResult execute(String jobName) {
    		IExecutorTemplate executor = jobs.get(jobName);
    		return addExecution(executor.createExecutor());
    }

	protected DispatchingResult addExecution(IExecutor executor) {
		DispatchingResult dispatcherResult = new DispatchingResult(executor.getJobDefinition());
        ExecutionMode executionMode = executor.getJobDefinition().getExecutionMode();
        DispatcherResult result = DispatcherResult.EXECUTED;
        boolean canBeExecuted = ExecutionMode.PARALLEL.equals(executionMode) || !checkRunning(executor);
        if (canBeExecuted) {
            LOG.debug("Starting new instance of  Job {}.", executor.getJobName());
            Thread execution = new Thread(executor);
            runningExecutors.put(executor.getJobName(), new WeakReference<Thread>(execution));
            execution.start();
        } else {
            LOG.debug("Skipping Job {}, already running!", executor.getJobName());
            result = DispatcherResult.DROPPED_ALREADY_RUNNING;
        }
        dispatcherResult.setDispatcherResult(result);
        return dispatcherResult;
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