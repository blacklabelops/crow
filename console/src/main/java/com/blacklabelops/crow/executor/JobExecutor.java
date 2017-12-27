package com.blacklabelops.crow.executor;

import com.blacklabelops.crow.executor.console.*;
import com.blacklabelops.crow.logger.IJobLogger;
import com.blacklabelops.crow.reporter.IJobReporter;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.*;

public class JobExecutor implements IExecutor {

    private final static int RETURN_CODE_OKAY = 0;

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

    public JobExecutor(IJobDefinition definition, List<IJobReporter> reporter, List<IJobLogger> logger)  {
        super();
        jobName = definition.getJobName();
        jobDefinition = definition;
        if (reporter != null) {
            reporter
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(report -> jobReporter.add(report));
        }
        if (logger != null) {
                logger
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(log -> jobLogger.add(log));
        }
    }

    @Override
    public void run() {
        jobLogger.forEach(IJobLogger::initializeLogger);
        try {
            ExecutorConsole executor = new ExecutorConsole();
            createDefaultOutputFiles(executor);
            startLogTrailing();
            this.setStartingTime(LocalDateTime.now());
            jobReporter.forEach(reporter -> reporter.startingJob(this));
            executor.execute(jobDefinition);
            returnCode = executor.getReturnCode();
            this.setFinishingTime(LocalDateTime.now());
            jobReporter.forEach(reporter -> reporter.finishedJob(this));
            if (RETURN_CODE_OKAY != returnCode) {
                jobReporter.forEach(reporter -> reporter.failingJob(this));
            }
            stopLogTrailing();
            deleteOutputFiles();
        } finally {
            jobLogger.forEach(IJobLogger::finishLogger);
        }
    }

    private void startLogTrailing() {
        outputFileReader = new OutputReader(outputFile, jobLogger.stream().map(IJobLogger::getInfoLogConsumer).filter(Objects::nonNull).collect(toList()));
        outputReaderThread = new Thread(outputFileReader);
        outputReaderThread.start();
        errorFileReader = new OutputReader(errorFile, jobLogger.stream().map(IJobLogger::getErrorLogConsumer).filter(Objects::nonNull).collect(toList()));
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

    @Override
    public List<IJobReporter> getReporter() {
        List<IJobReporter> copy = new ArrayList<>();
        Collections.copy(jobReporter, copy);
        return copy;
    }

    @Override
    public ErrorMode getErrorMode() {
        return jobDefinition.getErrorMode();
    }
}
