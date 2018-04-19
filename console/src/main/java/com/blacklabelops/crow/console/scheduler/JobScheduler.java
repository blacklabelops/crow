package com.blacklabelops.crow.console.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.console.definition.ErrorMode;
import com.blacklabelops.crow.console.definition.JobId;
import com.blacklabelops.crow.console.dispatcher.AbstractDispatchingResult;
import com.blacklabelops.crow.console.executor.ExecutionResult;

public class JobScheduler implements IScheduler {

	private static Logger LOG = LoggerFactory.getLogger(JobScheduler.class);

	private List<ScheduledJob> jobs = Collections.synchronizedList(new ArrayList<ScheduledJob>());

	private List<ScheduledJob> scheduledJobs = Collections.synchronizedList(new ArrayList<ScheduledJob>());

	private List<ScheduledJob> failedJobs = Collections.synchronizedList(new ArrayList<ScheduledJob>());

	public JobScheduler() {
		super();
	}

	@Override
	public void addJob(ScheduledJob job) {
		jobs.add(job);
		scheduledJobs.add(job);
		LOG.debug("Added job to the scheduler: {}", job);
	}

	@Override
	public void removeJob(ScheduledJob job) {
		jobs.remove(job);
		scheduledJobs.remove(job);
		failedJobs.remove(job);
		LOG.debug("Removed job from the scheduler: {}", job);
	}

	@Override
	public ScheduledJob getNextExecutableJob() {
		return scheduledJobs.stream().sorted(new JobComparator()).reduce((first, second) -> second).orElse(null);
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
		scheduledJobs.remove(foundJob);
		failedJobs.add(foundJob);
	}

	private Optional<ScheduledJob> findJob(JobId jobId) {
		return jobs.stream().filter(job -> job.getJobId().equals(jobId)).findFirst();
	}

}
