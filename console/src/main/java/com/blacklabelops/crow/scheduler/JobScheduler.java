package com.blacklabelops.crow.scheduler;

import com.blacklabelops.crow.definition.ErrorMode;
import com.blacklabelops.crow.dispatcher.DispatcherResult;
import com.blacklabelops.crow.executor.IExecutor;

import java.util.*;

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
    public Job getNextExecutableJob() {
        return scheduledJobs.stream().sorted(new JobComparator()).reduce((first,second) -> second).orElse(null);
    }

    @Override
    public void notifyFailingJob(IExecutor executor, DispatcherResult result) {
        handleFaultyJob(executor);
    }

    @Override
    public void notifyExecutionError(IExecutor executor, Integer returnCode) {
        handleFaultyJob(executor);
    }

    private void handleFaultyJob(IExecutor executor) {
        if (executor.getErrorMode() != null && !ErrorMode.CONTINUE.equals(executor.getErrorMode())) {
            Optional<Job> foundJob = findJobForExecutor(executor);
            foundJob.ifPresent(job -> unscheduleJob(job));
        }
    }

    private void unscheduleJob(Job foundJob) {
        scheduledJobs.remove(foundJob);
        failedJobs.add(foundJob);
    }

    private Optional<Job> findJobForExecutor(IExecutor executor) {
        return jobs.stream().filter(job -> job.getJobName().equals(executor.getJobName())).findFirst();
    }


}
