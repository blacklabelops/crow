package com.blacklabelops.crow.executor;

import com.blacklabelops.crow.executor.console.*;
import com.blacklabelops.crow.logger.IJobLogger;
import com.blacklabelops.crow.reporter.DummyReporter;
import com.blacklabelops.crow.reporter.IJobReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * Created by steffenbleul on 21.12.16.
 */
public class SimpleConsole implements IExecutor {

    private IJobReporter jobReporter = new DummyReporter();

    private final IJobDefinition jobDefinition;

    private IJobLogger jobLogger;

    private final FileAccessor fileAccessor = new FileAccessor();

    private final String jobName;

    private Path outputFile;

    private Path errorFile;

    private OutputReader outputFileReader;

    private Thread outputReaderThread;

    private OutputReader errorFileReader;

    private Thread errorReaderThread;

    private LocalDateTime startingTime;

    private LocalDateTime finishingTime;

    public SimpleConsole(IJobDefinition definition, IJobReporter reporter, IJobLogger logger)  {
        super();
        jobName = definition.getJobName();
        jobDefinition = definition;
        if (reporter != null) {
            jobReporter = reporter;
        }
        jobLogger = logger;
    }

    @Override
    public void run() {
        if (jobLogger != null) {
            jobLogger.initializeLogger();
        }
        try {
            ExecutorConsole executor = new ExecutorConsole();
            if (jobLogger != null) {
                createDefaultOutputFiles(executor);
                startLogTrailing();
            }
            this.setStartingTime(LocalDateTime.now());
            jobReporter.startingJob(this);
            executor.execute(jobDefinition);
            this.setFinishingTime(LocalDateTime.now());
            jobReporter.finishedJob(this);
            if (jobLogger != null) {
                stopLogTrailing();
                deleteOutputFiles();
            }
        } finally {
            if (jobLogger != null) {
                jobLogger.finishLogger();
            }
        }
    }

    private void startLogTrailing() {
        outputFileReader = new OutputReader(outputFile, jobLogger.getInfoLogConsumer());
        outputReaderThread = new Thread(outputFileReader);
        outputReaderThread.start();
        errorFileReader = new OutputReader(errorFile, jobLogger.getErrorLogConsumer());
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

    public LocalDateTime getStartingTime() {
        return startingTime;
    }

    private void setStartingTime(LocalDateTime startingTime) {
        this.startingTime = startingTime;
    }

    public LocalDateTime getFinishingTime() {
        return finishingTime;
    }

    private void setFinishingTime(LocalDateTime finishingTime) {
        this.finishingTime = finishingTime;
    }
}
