package com.blacklabelops.crow.executor;

import com.blacklabelops.crow.executor.console.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.nio.file.Path;

/**
 * Created by steffenbleul on 21.12.16.
 */
public class SimpleConsole implements IExecutor {

    public static final String MDC_JOBNAME = "jobname";

    private Logger jobLogger;

    private final DefinitionConsole jobDefinition;

    private final FileAccessor fileAccessor;

    private final String jobName;

    private Path outputFile;

    private Path errorFile;

    private OutputReader outputFileReader;

    private Thread outputReaderThread;

    private OutputReader errorFileReader;

    private Thread errorReaderThread;

    public SimpleConsole(String name, DefinitionConsole definition, FileAccessor accessor) {
        super();
        jobLogger = LoggerFactory.getLogger(name);
        jobDefinition = definition;
        fileAccessor = accessor;
        jobName = name;
    }

    @Override
    public void run() {
        MDC.put(MDC_JOBNAME,jobName);
        try {
            ExecutorConsole executor = new ExecutorConsole();
            createDefaultOutputFiles(executor);
            startLogTrailing();
            executor.execute(jobDefinition);
            stopLogTrailing();
            deleteOutputFiles();
        } finally {
            MDC.remove(MDC_JOBNAME);
        }
    }

    private void startLogTrailing() {
        outputFileReader = new OutputReader(outputFile, new LogInfoConsumer(jobLogger));
        outputReaderThread = new Thread(outputFileReader);
        outputReaderThread.start();
        errorFileReader = new OutputReader(errorFile, new LogErrorConsumer(jobLogger));
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
}
