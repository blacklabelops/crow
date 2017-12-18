package com.blacklabelops.crow.reporter;

import com.blacklabelops.crow.executor.IExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleReporter implements IJobReporter {

    private Logger logger = LoggerFactory.getLogger(ConsoleReporter.class);

    private String startMessageFormat = "Starting job %s at ";

    public ConsoleReporter() {
        super();
    }

    @Override
    public void startingJob(IExecutor pExecutingJob) {

    }

    @Override
    public void finishedJob(IExecutor pExecutingJob) {

    }
}
