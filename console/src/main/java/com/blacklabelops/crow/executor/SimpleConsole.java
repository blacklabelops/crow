package com.blacklabelops.crow.executor;

import com.blacklabelops.crow.executor.console.*;
import com.blacklabelops.crow.logger.IJobLogger;
import com.blacklabelops.crow.reporter.IJobReporter;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by steffenbleul on 21.12.16.
 */
public class SimpleConsole implements IExecutor {

    private final String jobName;

    private final IJobDefinition jobDefinition;

    private final List<IJobReporter> jobReporter = new ArrayList<>();

    private final List<IJobLogger> jobLogger = new ArrayList<>();

    private final FileAccessor fileAccessor = new FileAccessor();

    private Path outputFile;

    private Path errorFile;

    private OutputReader outputFileReader;

    private Thread outputReaderThread;

    private OutputReader errorFileReader;

    private Thread errorReaderThread;

    private LocalDateTime startingTime;

    private LocalDateTime finishingTime;

    private Integer returnCode;

    public SimpleConsole(IJobDefinition definition, List<IJobReporter> reporter, List<IJobLogger> logger)  {
        super();
        jobName = definition.getJobName();
        jobDefinition = definition;
        if (reporter != null) {
            reporter.stream().filter(report -> report != null).forEach(report -> jobReporter.add(report));
        }
        if (logger != null) {
            logger.stream().filter(log -> log != null).forEach(log -> jobLogger.add(log));
        }
    }

    @Override
    public void run() {
        if (jobLogger != null) {
            jobLogger.forEach(logger -> logger.initializeLogger());
        }
        try {
            ExecutorConsole executor = new ExecutorConsole();
            if (jobLogger != null) {
                createDefaultOutputFiles(executor);
                startLogTrailing();
            }
            this.setStartingTime(LocalDateTime.now());
            jobReporter.forEach(reporter -> reporter.startingJob(this));
            executor.execute(jobDefinition);
            returnCode = executor.getReturnCode();
            this.setFinishingTime(LocalDateTime.now());
            jobReporter.forEach(reporter -> reporter.finishedJob(this));
            if (jobLogger != null) {
                stopLogTrailing();
                deleteOutputFiles();
            }
        } finally {
            if (jobLogger != null) {
                jobLogger.forEach(logger -> logger.finishLogger());
            }
        }
    }

    private void startLogTrailing() {
        //jobLogger.getInfoLogConsumer()
        List<Consumer<String>> infoLogger = new ArrayList<>(jobLogger.size());
        jobLogger.stream().filter(logger -> logger.getInfoLogConsumer() != null).forEach(logger -> infoLogger.add(logger.getInfoLogConsumer()));
        outputFileReader = new OutputReader(outputFile, infoLogger);
        outputReaderThread = new Thread(outputFileReader);
        outputReaderThread.start();
        List<Consumer<String>> errorLogger = new ArrayList<>(jobLogger.size());
        jobLogger.stream().filter(logger -> logger.getErrorLogConsumer() != null).forEach(logger -> errorLogger.add(logger.getErrorLogConsumer()));
        errorFileReader = new OutputReader(errorFile, errorLogger);
        errorReaderThread = new Thread(errorFileReader);
        errorReaderThread.start();
    }

    private void stopLogTrailing() {
        stopFileTrailing(outputFileReader, outputReaderThread);
        stopFileTrailing(errorFileReader, errorReaderThread);
    }

    private void stopFileTrailing(OutputReader reader, Thread thread) {
        reader.stop();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException("Cannot join outputReader",e);
        }
    }

    private void deleteOutputFiles() {
        fileAccessor.deleteFile(outputFile);
        fileAccessor.deleteFile(errorFile);
    }

    private void createDefaultOutputFiles(ExecutorConsole executor) {
        outputFile = fileAccessor.createTempFile("ExecutorConsole","OutputFile");
        errorFile = fileAccessor.createTempFile("ExecutorConsole","ErrorFile");
        executor.setOutputFile(outputFile.toFile());
        executor.setOutputErrorFile(errorFile.toFile());
    }

    public String getJobName() {
        return jobName;
    }

    @Override
    public ExecutionMode getExecutionMode() {
        return jobDefinition.getExecutionMode();
    }

    @Override
    public LocalDateTime getStartingTime() {
        return startingTime;
    }

    private void setStartingTime(LocalDateTime startingTime) {
        this.startingTime = startingTime;
    }

    @Override
    public LocalDateTime getFinishingTime() {
        return finishingTime;
    }

    private void setFinishingTime(LocalDateTime finishingTime) {
        this.finishingTime = finishingTime;
    }

    @Override
    public Integer getReturnCode() {
        return this.returnCode;
    }
}
