package com.blacklabelops.crow.console.scheduler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.console.definition.ErrorMode;
import com.blacklabelops.crow.console.definition.JobId;
import com.blacklabelops.crow.console.dispatcher.AbstractDispatchingResult;
import com.blacklabelops.crow.console.executor.ExecutionResult;

public class JobScheduler implements IScheduler {

	private static Logger LOG = LoggerFactory.getLogger(JobScheduler.class);

	private Map<JobId, ScheduledJob> jobs = Collections.synchronizedMap(new HashMap<>());

	private Map<JobId, ScheduledJob> scheduledJobs = Collections.synchronizedMap(new HashMap<>());

	private Map<JobId, ScheduledJob> failedJobs = Collections.synchronizedMap(new HashMap<>());

	public JobScheduler() {
		super();
	}

	@Override
	public void addJob(ScheduledJob job) {
		jobs.put(job.getJobId(), job);
		scheduledJobs.put(job.getJobId(), job);
		LOG.debug("Added job to the scheduler: {}", job);
	}

	@Override
	public void removeJob(JobId job) {
		jobs.remove(job);
		scheduledJobs.remove(job);
		failedJobs.remove(job);
		LOG.debug("Removed job from the scheduler: {}", job);
	}

	@Override
	public void updateJob(JobId jobId, ScheduledJob job) {
		removeJob(jobId);
		addJob(job);
	}

	@Override
	public ScheduledJob getNextExecutableJob() {
		return scheduledJobs.values().stream().sorted(new JobComparator()).reduce((first, second) -> second).orElse(
				null);
	}

	@Override
	public void notifyDispatchingError(AbstractDispatchingResult dispatcherResult) {
		if (dispatcherResult.getJobName() != null) {
			decideUnscheduling(dispatcherResult.getJobDefinition().getErrorMode(), dispatcherResult.getJobId());
		}
	}

	private void decideUnscheduling(ErrorMode errorMode, JobId jobId) {
		if (errorMode != null && !ErrorMode.CONTINUE.equals(errorMode)) {
			Optional<ScheduledJob> foundJob = findJob(jobId);
			foundJob.ifPresent(job -> unscheduleJob(job));
		}
	}

	@Override
	public void notifyExecutionError(ExecutionResult executionResult) {
		decideUnscheduling(executionResult.getJobDefinition().getErrorMode(), executionResult.getJobId());
	}

	private void unscheduleJob(ScheduledJob foundJob) {
		scheduledJobs.remove(foundJob.getJobId());
		failedJobs.put(foundJob.getJobId(), foundJob);
	}

	private Optional<ScheduledJob> findJob(JobId jobId) {
		return Optional.ofNullable(jobs.get(jobId));
	}

}
