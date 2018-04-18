package com.blacklabelops.crow.console.reporter;

public class ConsoleReporterFactory implements IJobReporterFactory {


    public ConsoleReporterFactory() {
        super();
    }

    @Override
    public IJobReporter createInstance() {
        return new ConsoleReporter();
    }
}
