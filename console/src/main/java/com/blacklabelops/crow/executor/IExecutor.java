package com.blacklabelops.crow.executor;

/**
 * Created by steffenbleul on 22.12.16.
 */
public interface IExecutor extends Runnable {

    public void run();

    public String getJobName();

    public ExecutionMode getExecutionMode();
}
