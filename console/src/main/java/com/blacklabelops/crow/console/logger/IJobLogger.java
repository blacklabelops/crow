package com.blacklabelops.crow.console.logger;

import java.io.OutputStream;

public interface IJobLogger {

	public void initializeLogger();

	public void finishLogger();

	public OutputStream getInfoLogConsumer();

	public OutputStream getErrorLogConsumer();
}
