package com.blacklabelops.crow.console.executor.console;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.executor.ExecutorException;

class LocalConsole {

	public static final Logger LOG = LoggerFactory.getLogger(LocalConsole.class);

	private File inputFile;

	private File outputFile;

	private File outputErrorFile;

	private Integer returnCode;

	private boolean timedOut;

	public LocalConsole() {
		super();
	}

	private void checkDefinition(Job executionDefinition) {
		if (executionDefinition == null)
			throw new ExecutorException("Executor has no job definition!");
		if (!executionDefinition.getCommand().isPresent())
			throw new ExecutorException("Executor has no command specified");
	}

	public void execute(Job executionDefinition) {
		checkDefinition(executionDefinition);
		boolean preResult = true;
		if (executionDefinition.getPreCommand().isPresent() && !executionDefinition.getPreCommand().get().isEmpty()) {
			preResult = executePreCommands(executionDefinition);
		}
		boolean result = preResult ? executeCommands(executionDefinition) : preResult;
		if (result && executionDefinition.getPostCommand().isPresent() && !executionDefinition.getPostCommand().get()
				.isEmpty()) {
			executePostCommands(executionDefinition);
		}
	}

	private void executePostCommands(Job executionDefinition) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(executionDefinition.getPostCommand().get());
		extendEnvironmentVariables(processBuilder, executionDefinition);
		setWorkingDirectory(executionDefinition, processBuilder);
		prepareRedirects(processBuilder);
		if (executionDefinition.getTimeoutMinutes().isPresent()) {
			executeCommandWithTimeout(processBuilder, executionDefinition.getTimeoutMinutes().get());
		} else {
			executeCommand(processBuilder);
		}
	}

	private void setWorkingDirectory(Job executionDefinition, ProcessBuilder processBuilder) {
		if (executionDefinition.getWorkingDir().isPresent()) {
			File workingDirectory = new File(executionDefinition.getWorkingDir().get());
			if (workingDirectory.exists() && workingDirectory.isDirectory()) {
				processBuilder.directory(workingDirectory);
			}
		}
	}

	private boolean executePreCommands(Job executionDefinition) {
		boolean result = false;
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(executionDefinition.getPreCommand().get());
		extendEnvironmentVariables(processBuilder, executionDefinition);
		setWorkingDirectory(executionDefinition, processBuilder);
		prepareRedirects(processBuilder);
		if (executionDefinition.getTimeoutMinutes().isPresent()) {
			executeCommandWithTimeout(processBuilder, executionDefinition.getTimeoutMinutes().get());
		} else {
			executeCommand(processBuilder);
		}
		if (!timedOut && Integer.valueOf(0).equals(returnCode)) {
			result = true;
		}
		return result;
	}

	private boolean executeCommands(Job executionDefinition) {
		boolean result = false;
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(takeOverCommands(executionDefinition));
		extendEnvironmentVariables(processBuilder, executionDefinition);
		setWorkingDirectory(executionDefinition, processBuilder);
		prepareRedirects(processBuilder);
		if (executionDefinition.getTimeoutMinutes().isPresent()) {
			executeCommandWithTimeout(processBuilder, executionDefinition.getTimeoutMinutes().get());
		} else {
			executeCommand(processBuilder);
		}
		if (!timedOut && Integer.valueOf(0).equals(returnCode)) {
			result = true;
		}
		return result;
	}

	private List<String> takeOverCommands(Job executionDefinition) {
		List<String> command = executionDefinition.getCommand().get();
		LOG.debug("Executing command: {}", command);
		return command;
	}

	private void executeCommand(ProcessBuilder processBuilder) {
		Process process = null;
		try {
			process = processBuilder.start();
			returnCode = process.waitFor();
		} catch (IOException | InterruptedException e) {
			throw new ExecutorException("Error executing job", e);
		}
	}

	private void executeCommandWithTimeout(ProcessBuilder processBuilder, int minutes) {
		Process process = null;
		try {
			process = processBuilder.start();
			this.timedOut = !process.waitFor(minutes, TimeUnit.MINUTES);
			if (!timedOut) {
				this.returnCode = process.exitValue();
			}
		} catch (IOException | InterruptedException e) {
			throw new ExecutorException("Error executing job", e);
		}
	}

	private void prepareRedirects(ProcessBuilder processBuilder) {
		if (outputFile != null) {
			processBuilder.redirectOutput(Redirect.appendTo(outputFile));
		}
		if (outputErrorFile != null) {
			processBuilder.redirectError(Redirect.appendTo(outputErrorFile));
		}
		if (inputFile != null) {
			processBuilder.redirectInput(inputFile);
		}
	}

	private void extendEnvironmentVariables(ProcessBuilder processBuilder, Job executionDefinition) {
		Map<String, String> environmentVariables = processBuilder.environment();
		if (executionDefinition.getEnvironmentVariables().isPresent() && !executionDefinition.getEnvironmentVariables()
				.get()
				.isEmpty()) {
			environmentVariables.putAll(executionDefinition.getEnvironmentVariables().get());
		}
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	public void setOutputErrorFile(File outputErrorFile) {
		this.outputErrorFile = outputErrorFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public Integer getReturnCode() {
		return this.returnCode;
	}

	public boolean isTimedOut() {
		return this.timedOut;
	}

}
