package com.blacklabelops.crow.dispatcher;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.definition.ExecutionMode;
import com.blacklabelops.crow.executor.IExecutor;
import com.blacklabelops.crow.executor.IExecutorTemplate;
import com.blacklabelops.crow.logger.IJobLogger;
import com.blacklabelops.crow.reporter.IJobReporter;

public class Dispatcher implements IDispatcher {

	private static Logger LOG = LoggerFactory.getLogger(Dispatcher.class);

	private Map<String, IExecutorTemplate> jobs = Collections.synchronizedMap(new HashMap<>());

	private Map<String, WeakReference<Thread>> runningExecutors = Collections.synchronizedMap(new HashMap<>());

	public Dispatcher() {
		super();
	}

	@Override
	public void addJob(IExecutorTemplate executorTemplate) {
		jobs.put(executorTemplate.getJobId(), executorTemplate);
	}

	@Override
	public void removeJob(String jobId) {
		jobs.remove(jobId);
	}

	@Override
	public DispatchingResult execute(String jobId) {
		IExecutorTemplate executor = jobs.get(jobId);
		return addExecution(executor.createExecutor());
	}

	@Override
	public DispatchingResult execute(String jobId, List<IJobReporter> reporters, List<IJobLogger> loggers) {
		IExecutorTemplate executorTemplate = jobs.get(jobId);
		IExecutor executor = executorTemplate.createExecutor();
		if (loggers != null) {
			executor.addLogger(loggers);
		}
		if (reporters != null) {
			executor.addReporter(reporters);
		}
		return addExecution(executor);
	}

	@Override
	public void testExecute(String jobId, List<IJobReporter> reporters, List<IJobLogger> loggers) {
		IExecutorTemplate executorTemplate = jobs.get(jobId);
		IExecutor executor = executorTemplate.createExecutor();
		executor.deleteReporters();
		executor.deleteLoggers();
		if (loggers != null) {
			executor.addLogger(loggers);
		}
		if (reporters != null) {
			executor.addReporter(reporters);
		}
		Thread execution = new Thread(executor);
		execution.start();
	}

	protected DispatchingResult addExecution(IExecutor executor) {
		DispatchingResult dispatcherResult = new DispatchingResult(executor.getJobDefinition());
		ExecutionMode executionMode = executor.getJobDefinition().getExecutionMode();
		DispatcherResult result = DispatcherResult.EXECUTED;
		boolean canBeExecuted = ExecutionMode.PARALLEL.equals(executionMode) || !checkRunning(executor);
		if (canBeExecuted) {
			LOG.debug("Starting new instance of  Job {}.", executor.getJobId());
			Thread execution = new Thread(executor);
			runningExecutors.put(executor.getJobId(), new WeakReference<Thread>(execution));
			execution.start();
		} else {
			LOG.debug("Skipping Job {}, already running!", executor.getJobId());
			result = DispatcherResult.DROPPED_ALREADY_RUNNING;
		}
		dispatcherResult.setDispatcherResult(result);
		return dispatcherResult;
	}

	protected boolean checkRunning(IExecutor executor) {
		boolean alreadyRunning = true;
		WeakReference<Thread> reference = runningExecutors.get(executor.getJobId());
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
