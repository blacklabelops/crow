package com.blacklabelops.crow.scheduler;


import com.blacklabelops.crow.executor.IExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Created by steffenbleul on 20.12.16.
 */
public class SingleJobScheduler implements Runnable {

    public static final int MINIMAL_TIME_STEP = 100;

    public static final Logger LOG = LoggerFactory.getLogger(SingleJobScheduler.class);

    private IExecutor jobExecutor;

    private IExecutionTime executionTime;

    private boolean keepRunning = true;

    public SingleJobScheduler(IExecutor executor, IExecutionTime executionTimes) {
        super();
        jobExecutor = executor;
        executionTime = executionTimes;
    }

    @Override
    public void run() {
        ChronoUnit chronoUnit = ChronoUnit.MILLIS;
        do {
            ZonedDateTime nextExecution = executionTime.nextExecution(ZonedDateTime.now());
            boolean waitFornextExecution = true;
            while (waitFornextExecution && keepRunning) {
                long timeToNextExecution = chronoUnit.between(ZonedDateTime.now(), nextExecution);
                if (timeToNextExecution <= 0) {
                    LOG.debug("Executing job {}.",jobExecutor.getJobName());
                    jobExecutor.run();
                    waitFornextExecution = false;
                } else {
                    LOG.trace("Waiting for next job execution in {} ms.", timeToNextExecution);
                    waitForNextExecution(timeToNextExecution);
                }
            }
        } while(keepRunning);
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
