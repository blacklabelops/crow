package com.blacklabelops.crow.console.executor;

import com.blacklabelops.crow.console.definition.JobId;

public interface IExecutorTemplate {

	public IExecutor createExecutor();

	public JobId getJobId();

}
