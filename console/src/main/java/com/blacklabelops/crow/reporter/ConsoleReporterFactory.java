package com.blacklabelops.crow.reporter;

public class ConsoleReporterFactory implements IJobReporterFactory {


    public ConsoleReporterFactory() {
        super();
    }

    @Override
    public IJobReporter createInstance() {
        return new ConsoleReporter();
    }
}
