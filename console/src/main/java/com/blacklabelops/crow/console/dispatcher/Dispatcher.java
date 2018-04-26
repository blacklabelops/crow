package com.blacklabelops.crow.console.dispatcher;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.console.definition.ExecutionMode;
import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.definition.JobId;
import com.blacklabelops.crow.console.executor.IExecutor;
import com.blacklabelops.crow.console.executor.IExecutorTemplate;
import com.blacklabelops.crow.console.logger.IJobLogger;
import com.blacklabelops.crow.console.reporter.IJobReporter;

public class Dispatcher implements IDispatcher {

	private static Logger LOG = LoggerFactory.getLogger(Dispatcher.class);

	private Map<JobId, IExecutorTemplate> jobs = Collections.synchronizedMap(new HashMap<>());

	private Map<JobId, WeakReference<Thread>> runningExecutors = Collections.synchronizedMap(new HashMap<>());

	public Dispatcher() {
		super();
	}

	@Override
	public void addJob(IExecutorTemplate executorTemplate) {
		jobs.put(executorTemplate.getJobId(), executorTemplate);
	}

	@Override
	public void removeJob(JobId jobId) {
		jobs.remove(jobId);
	}

	@Override
	public DispatchingResult execute(JobId jobId) {
		IExecutorTemplate executor = jobs.get(jobId);
		return addExecution(executor.createExecutor());
	}

	@Override
	public DispatchingResult execute(JobId jobId, List<IJobReporter> reporters,
			List<IJobLogger> loggers) {
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
	public void testExecute(JobId jobId, List<IJobReporter> reporters, List<IJobLogger> loggers) {
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
		ExecutionMode executionMode = executor.getJobDefinition().getExecutorMode();
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
		return DispatchingResult.of(Job.copyOf(executor.getJobDefinition()), result);
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
