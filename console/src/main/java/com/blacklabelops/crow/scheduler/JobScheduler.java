package com.blacklabelops.crow.scheduler;

import java.util.*;

/**
 * Created by steffenbleul on 22.12.16.
 */
public class JobScheduler implements IScheduler {

    private List<Job> jobs = Collections.synchronizedList(new ArrayList<Job>());

    public JobScheduler() {
        super();
    }

    @Override
    public void addJob(Job job) {
        jobs.add(job);
    }

    @Override
    public Job getNextExecutableJob() {
        Job nextExecutableJob = null;
        if (!jobs.isEmpty()) {
            nextExecutableJob = jobs.stream().sorted(new JobComparator()).reduce((first,second) -> second).orElse(null);
        }
        return nextExecutableJob;
    }
}
