package com.blacklabelops.crow.reporter;

import com.blacklabelops.crow.executor.IExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ConsoleReporter implements IJobReporter {

    private Logger logger = LoggerFactory.getLogger(ConsoleReporter.class);

    private String startMessageFormat = "Starting job %s at %s.";

    private String finishMessageFormat = "Finished job %s with return code %d at %s.";

    private String failingMessageFormat = "Failed executing job %s at %s.";

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ConsoleReporter() {
        super();
    }

    @Override
    public void startingJob(IExecutor pExecutingJob) {
        String time = formatter.format(pExecutingJob.getStartingTime());
        String message = String.format(startMessageFormat, pExecutingJob.getJobName(), time);
        logger.info(message);
    }

    @Override
    public void finishedJob(IExecutor pExecutingJob) {
        String time = formatter.format(pExecutingJob.getFinishingTime());
        String message = String.format(finishMessageFormat, pExecutingJob.getJobName(), pExecutingJob.getReturnCode(), time);
        logger.info(message);
    }

    @Override
    public void failingJob(IExecutor executingJob) {
        String time = formatter.format(LocalTime.now());
        String message = String.format(failingMessageFormat, executingJob.getJobName(), time);
        logger.info(message);
    }
}
