package com.blacklabelops.crow.executor.docker;

import static java.util.stream.Collectors.toList;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.executor.ExecutionResult;
import com.blacklabelops.crow.executor.FileAccessor;
import com.blacklabelops.crow.executor.IExecutor;
import com.blacklabelops.crow.executor.OutputReader;
import com.blacklabelops.crow.logger.IJobLogger;
import com.blacklabelops.crow.reporter.IJobReporter;

public class DockerExecutor implements IExecutor {

    private final static int RETURN_CODE_OKAY = 0;

    private final String jobName;

    private final JobDefinition jobDefinition;

    private final List<IJobReporter> jobReporter = new ArrayList<>();

    private final List<IJobLogger> jobLogger = new ArrayList<>();

    private final FileAccessor fileAccessor = new FileAccessor();

    private Path outputFile;

    private Path errorFile;

    private OutputReader outputFileReader;

    private Thread outputReaderThread;

    private OutputReader errorFileReader;

    private Thread errorReaderThread;
    
    private ExecutionResult executionResult;

    public DockerExecutor(JobDefinition definition, List<IJobReporter> reporter, List<IJobLogger> logger)  {
        super();
        jobName = definition.getJobName();
        jobDefinition = new JobDefinition(definition);
        this.executionResult = new ExecutionResult(jobDefinition);
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
            RemoteContainer executor = new RemoteContainer();
            createDefaultOutputFiles(executor);
            startLogTrailing();
            this.executionResult.setStartingTime(LocalDateTime.now());
            jobReporter.parallelStream().forEach(reporter -> reporter.startingJob(new ExecutionResult(this.executionResult)));
            executor.execute(jobDefinition);
            this.executionResult.setTimedOut(executor.isTimedOut());
            this.executionResult.setReturnCode(executor.getReturnCode());
            this.executionResult.setFinishingTime(LocalDateTime.now());
            stopLogTrailing();
            jobReporter.parallelStream().forEach(reporter -> reporter.finishedJob(new ExecutionResult(this.executionResult)));
            if (!this.executionResult.isTimedOut()) {
        			if (RETURN_CODE_OKAY != this.executionResult.getReturnCode()) {
        				jobReporter.parallelStream().forEach(reporter -> reporter.failingJob(new ExecutionResult(this.executionResult)));
                 }
            } else {
            		jobReporter.parallelStream().forEach(reporter -> reporter.failingJob(new ExecutionResult(this.executionResult)));
            }
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

    private void createDefaultOutputFiles(RemoteContainer executor) {
        outputFile = fileAccessor.createTempFile("ExecutorConsole","OutputFile");
        errorFile = fileAccessor.createTempFile("ExecutorConsole","ErrorFile");
        executor.setOutputFile(outputFile.toFile());
        executor.setOutputErrorFile(errorFile.toFile());
    }

    public String getJobName() {
        return jobName;
    }

	@Override
    public List<IJobReporter> getReporter() {
        List<IJobReporter> copy = new ArrayList<>();
        Collections.copy(jobReporter, copy);
        return copy;
    }

	@Override
	public JobDefinition getJobDefinition() {
		return new JobDefinition(jobDefinition);
	}

	@Override
	public ExecutionResult getExecutionResult() {
		return new ExecutionResult(this.executionResult);
	}
    
	
    
}
