package com.blacklabelops.crow.logger;

import java.util.function.Consumer;

public interface IJobLogger {

    public void initializeLogger();

    public void finishLogger();

    public Consumer<String> getInfoLogConsumer();

    public Consumer<String> getErrorLogConsumer();
}
