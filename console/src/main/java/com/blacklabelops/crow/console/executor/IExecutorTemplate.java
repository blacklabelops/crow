package com.blacklabelops.crow.console.executor;

public interface IExecutorTemplate {

	public IExecutor createExecutor();

	public String getJobId();

}
