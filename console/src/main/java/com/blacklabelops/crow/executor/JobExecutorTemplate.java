package com.blacklabelops.crow.executor;

import com.blacklabelops.crow.executor.console.IJobDefinition;
import com.blacklabelops.crow.logger.IJobLogger;
import com.blacklabelops.crow.logger.IJobLoggerFactory;
import com.blacklabelops.crow.reporter.IJobReporter;
import com.blacklabelops.crow.reporter.IJobReporterFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JobExecutorTemplate implements IExecutorTemplate {

    private final String jobName;

    private final IJobDefinition jobDefinition;

    private final List<IJobReporterFactory> jobReporterFactories = new ArrayList<>();

    private final List<IJobLoggerFactory> jobLoggerFactories = new ArrayList<>();

    public JobExecutorTemplate(IJobDefinition definition, List<IJobReporterFactory> reporter, List<IJobLoggerFactory> logger) {
        super();
        jobName = definition.getJobName();
        jobDefinition = definition;
        if (reporter != null) {
            reporter
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(report -> jobReporterFactories.add(report));
        }
        if (logger != null) {
            logger
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(log -> jobLoggerFactories.add(log));
        }
    }

    public IExecutor createExecutor() {
        List<IJobReporter> reporters = createReporters();
        List<IJobLogger> logger = createLoggers();
        return new JobExecutor(jobDefinition, reporters, logger);
    }

    private List<IJobLogger> createLoggers() {
        List<IJobLogger> loggers = new ArrayList<>(jobLoggerFactories.size());
        jobLoggerFactories.stream().forEach(loggerFactory -> loggers.add(loggerFactory.createInstance()));
        return loggers;
    }

    private List<IJobReporter> createReporters() {
        List<IJobReporter> reporters = new ArrayList<>(jobReporterFactories.size());
        jobReporterFactories.stream().forEach(reporterFactory -> reporters.add(reporterFactory.createInstance()));
        return reporters;
    }

    public String getJobName() {
        return jobName;
    }
}
