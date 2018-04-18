package com.blacklabelops.crow.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.blacklabelops.crow.definition.ErrorMode;
import com.blacklabelops.crow.dispatcher.DispatchingResult;
import com.blacklabelops.crow.executor.ExecutionResult;

public class JobScheduler implements IScheduler {

	private List<Job> jobs = Collections.synchronizedList(new ArrayList<Job>());

	private List<Job> scheduledJobs = Collections.synchronizedList(new ArrayList<Job>());

	private List<Job> failedJobs = Collections.synchronizedList(new ArrayList<Job>());

	public JobScheduler() {
		super();
	}

	@Override
	public void addJob(Job job) {
		jobs.add(job);
		scheduledJobs.add(job);
	}

	@Override
	public void removeJob(Job job) {
		jobs.remove(job);
		scheduledJobs.remove(job);
		failedJobs.remove(job);
	}

	@Override
	public Job getNextExecutableJob() {
		return scheduledJobs.stream().sorted(new JobComparator()).reduce((first, second) -> second).orElse(null);
	}

	@Override
	public void notifyDispatchingError(DispatchingResult dispatcherResult) {
		if (dispatcherResult.getJobName() != null) {
			decideUnscheduling(dispatcherResult.getJobDefinition().getErrorMode(), dispatcherResult.getJobName());
		}
	}

	private void decideUnscheduling(ErrorMode errorMode, String jobName) {
		if (errorMode != null && !ErrorMode.CONTINUE.equals(errorMode)) {
			Optional<Job> foundJob = findJob(jobName);
			foundJob.ifPresent(job -> unscheduleJob(job));
		}
	}

	@Override
	public void notifyExecutionError(ExecutionResult executionResult) {
		decideUnscheduling(executionResult.getJobDefinition().getErrorMode(), executionResult.getJobName());
	}

	private void unscheduleJob(Job foundJob) {
		scheduledJobs.remove(foundJob);
		failedJobs.add(foundJob);
	}

	private Optional<Job> findJob(String jobName) {
		return jobs.stream().filter(job -> job.getJobId().equals(jobName)).findFirst();
	}

}
