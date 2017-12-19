package com.blacklabelops.crow.executor;

import java.time.LocalDateTime;

/**
 * Created by steffenbleul on 22.12.16.
 */
public interface IExecutor extends Runnable {

    public void run();

    public String getJobName();

    public ExecutionMode getExecutionMode();

    public LocalDateTime getStartingTime();

    public LocalDateTime getFinishingTime();

    public Integer getReturnCode();
}
