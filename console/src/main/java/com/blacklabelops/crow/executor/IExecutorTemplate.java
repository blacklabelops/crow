package com.blacklabelops.crow.executor;

public interface IExecutorTemplate {

	public IExecutor createExecutor();

	public String getJobId();
}
