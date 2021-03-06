package com.blacklabelops.crow.console.executor.console;

import static java.util.stream.Collectors.toList;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.definition.JobId;
import com.blacklabelops.crow.console.executor.ExecutionResult;
import com.blacklabelops.crow.console.executor.FileAccessor;
import com.blacklabelops.crow.console.executor.IExecutor;
import com.blacklabelops.crow.console.executor.OutputReader;
import com.blacklabelops.crow.console.logger.IJobLogger;
import com.blacklabelops.crow.console.reporter.IJobReporter;

public class ConsoleExecutor implements IExecutor {

	private final static int RETURN_CODE_OKAY = 0;

	private final String jobName;

	private final JobId jobId;

	private final Job jobDefinition;

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

	public ConsoleExecutor(Job definition, List<IJobReporter> reporter, List<IJobLogger> logger) {
		super();
		jobName = definition.getName();
		jobId = definition.getId();
		jobDefinition = definition;
		this.executionResult = ExecutionResult.of(Job.copyOf(jobDefinition));
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
			LocalConsole executor = new LocalConsole();
			createDefaultOutputFiles(executor);
			startLogTrailing();
			this.executionResult = this.executionResult.withStartingTime(LocalDateTime.now());
			jobReporter.parallelStream().forEach(reporter -> reporter.startingJob(
					this.executionResult));
			executor.execute(jobDefinition);
			this.executionResult = this.executionResult
					.withTimedOut(Optional.ofNullable(executor.isTimedOut())) //
					.withReturnCode(Optional.ofNullable(executor.getReturnCode())) //
					.withFinishingTime(LocalDateTime.now());
		} finally {
			stopLogTrailing();
			deleteOutputFiles();
			jobLogger.forEach(IJobLogger::finishLogger);
		}
		reportResults();

	}

	private void reportResults() {
		jobReporter.parallelStream().forEach(reporter -> reporter.finishedJob(
				this.executionResult));
		if (!this.executionResult.isTimedOut().orElse(Boolean.FALSE)) {
			if (this.executionResult.getReturnCode().isPresent() && RETURN_CODE_OKAY != this.executionResult
					.getReturnCode().get()) {
				jobReporter.parallelStream().forEach(reporter -> reporter.failingJob(
						this.executionResult));
			}
		} else {
			jobReporter.parallelStream().forEach(reporter -> reporter.failingJob(
					this.executionResult));
		}
	}

	private void startLogTrailing() {
		outputFileReader = new OutputReader(outputFile, jobLogger.stream().map(IJobLogger::getInfoLogConsumer).filter(
				Objects::nonNull).collect(toList()));
		outputReaderThread = new Thread(outputFileReader);
		outputReaderThread.start();
		errorFileReader = new OutputReader(errorFile, jobLogger.stream().map(IJobLogger::getErrorLogConsumer).filter(
				Objects::nonNull).collect(toList()));
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
			throw new RuntimeException("Cannot join outputReader", e);
		}
	}

	private void deleteOutputFiles() {
		fileAccessor.deleteFile(outputFile);
		fileAccessor.deleteFile(errorFile);
	}

	private void createDefaultOutputFiles(LocalConsole executor) {
		outputFile = fileAccessor.createTempFile("ExecutorConsole", "OutputFile");
		errorFile = fileAccessor.createTempFile("ExecutorConsole", "ErrorFile");
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
	public Job getJobDefinition() {
		return jobDefinition;
	}

	@Override
	public ExecutionResult getExecutionResult() {
		return this.executionResult;
	}

	@Override
	public JobId getJobId() {
		return jobId;
	}

	@Override
	public void addReporter(List<IJobReporter> reporters) {
		this.jobReporter.addAll(reporters);
	}

	@Override
	public void addLogger(List<IJobLogger> loggers) {
		this.jobLogger.addAll(loggers);
	}

	@Override
	public void deleteReporters() {
		this.jobReporter.clear();
	}

	@Override
	public void deleteLoggers() {
		this.jobLogger.clear();
	}

}
