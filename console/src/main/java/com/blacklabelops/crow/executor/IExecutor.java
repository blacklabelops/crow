package com.blacklabelops.crow.executor;

import com.blacklabelops.crow.reporter.IJobReporter;

import java.time.LocalDateTime;
import java.util.List;

public interface IExecutor extends Runnable {

    public void run();

    public String getJobName();

    public ExecutionMode getExecutionMode();

    public LocalDateTime getStartingTime();

    public LocalDateTime getFinishingTime();

    public Integer getReturnCode();

    public List<IJobReporter> getReporter();

    public ErrorMode getErrorMode();
}
