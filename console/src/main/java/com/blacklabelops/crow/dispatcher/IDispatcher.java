package com.blacklabelops.crow.dispatcher;

import com.blacklabelops.crow.executor.IExecutor;
import com.blacklabelops.crow.executor.IExecutorTemplate;

public interface IDispatcher {

	public void addJob(IExecutorTemplate executorTemplate);

	public void removeJob(String jobName);
	
	IExecutor execute(String jobName);

}