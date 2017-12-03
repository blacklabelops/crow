package com.blacklabelops.crow.scheduler;

import com.blacklabelops.crow.executor.ExecutorPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Created by steffenbleul on 20.12.16.
 */
public class MultiJobScheduler implements Runnable {

    public static final int MINIMAL_TIME_STEP = 500;

    public static final Logger LOG = LoggerFactory.getLogger(MultiJobScheduler.class);

    private IScheduler jobScheduler;

    private ExecutorPool executorPool =  new ExecutorPool();

    private boolean keepRunning = true;

    public MultiJobScheduler(IScheduler scheduler) {
        super();
        jobScheduler = scheduler;
    }

    @Override
    public void run() {
        ChronoUnit chronoUnit = ChronoUnit.MILLIS;
        do {
            Job nextJob = jobScheduler.getNextExecutableJob();
            if (nextJob != null) {
                ZonedDateTime nextExecution = nextJob.getNextExecution();
                boolean waitFornextExecution = true;
                while (waitFornextExecution && keepRunning && checkJob(nextJob)) {
                    long timeToNextExecution = chronoUnit.between(ZonedDateTime.now(), nextExecution);
                    if (timeToNextExecution <= 0) {
                        LOG.debug("Executing job {}.", nextJob.getJobName());
                        executorPool.addExecution(nextJob.getExecutor());
                        nextJob.setLastExecution(ZonedDateTime.now());
                        waitFornextExecution = false;
                    } else {
                        LOG.trace("Waiting for next job execution in {} ms.", timeToNextExecution);
                        waitForNextExecution(timeToNextExecution);
                    }
                }
            } else {
                waitForNextExecution(MINIMAL_TIME_STEP);
            }
        } while(keepRunning);
    }

    private boolean checkJob(Job nextJob) {
        return jobScheduler.getNextExecutableJob() == nextJob;
    }

    private void waitForNextExecution(long nextExecutionMillis) {
        if (nextExecutionMillis > 0 && nextExecutionMillis < MINIMAL_TIME_STEP) {
            try {
                Thread.sleep(nextExecutionMillis);
            } catch (InterruptedException e) {
                throw new RuntimeException("Could not pause thread!", e);
            }
        } else {
            try {
                Thread.sleep(MINIMAL_TIME_STEP);
            } catch (InterruptedException e) {
                throw new RuntimeException("Could not default pause thread!", e);
            }
        }
    }

    public void stop() {
        LOG.trace("Stop signal received");
        keepRunning = false;
    }

}
