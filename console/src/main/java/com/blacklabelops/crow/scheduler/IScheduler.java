package com.blacklabelops.crow.scheduler;

/**
 * Created by steffenbleul on 23.12.16.
 */
public interface IScheduler {

    void addJob(Job job);

    Job getNextExecutableJob();
}
