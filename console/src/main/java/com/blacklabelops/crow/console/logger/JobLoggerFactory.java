package com.blacklabelops.crow.console.logger;

public class JobLoggerFactory implements IJobLoggerFactory {

	private final String jobLabel;

	public JobLoggerFactory(String jobLabel) {
		super();
		this.jobLabel = jobLabel;
	}

	@Override
	public IJobLogger createInstance() {
		return new JobLogLogger(this.jobLabel);
	}
}
