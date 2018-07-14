package com.blacklabelops.crow.console.executor.docker;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.executor.ExecutorException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ExecCreateParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.ExecState;

class RemoteContainer {

	public static final Logger LOG = LoggerFactory.getLogger(RemoteContainer.class);

	private OutputStream outStream;

	private OutputStream outErrorStream;

	private Integer returnCode;

	private boolean timedOut;

	private DockerClient dockerClient;

	private Job jobDefinition;

	private String container;

	public RemoteContainer() {
		super();
	}

	private void checkDefinition(Job executionDefinition) {
		if (executionDefinition == null)
			throw new ExecutorException("Executor has no job definition!");
		if (!executionDefinition.getCommand().isPresent())
			throw new ExecutorException("Executor has no command specified!");
		if (!executionDefinition.getContainerId().isPresent() && !executionDefinition.getContainerName().isPresent())
			throw new ExecutorException("No container had been defined!");
		jobDefinition = executionDefinition;
	}

	public void execute(Job executionDefinition) {
		checkDefinition(executionDefinition);
		evaluateContainer(executionDefinition);
		dockerClient = DockerClientFactory.initializeDockerClient();
		boolean preResult = true;
		try {
			if (executionDefinition.getPreCommand().isPresent() && !executionDefinition.getPreCommand().get()
					.isEmpty()) {
				preResult = executePreCommands(executionDefinition);
			}
			boolean result = preResult ? executeCommands(executionDefinition) : preResult;
			if (result && executionDefinition.getPostCommand().isPresent()
					&& !executionDefinition.getPostCommand().get().isEmpty()) {
				executePostCommands(executionDefinition);
			}
		} finally {
			IOUtils.closeQuietly(outStream);
			IOUtils.closeQuietly(outErrorStream);
		}
	}

	private void evaluateContainer(Job executionDefinition) {
		if (executionDefinition.getContainerName().isPresent()) {
			this.container = executionDefinition.getContainerName().get();
		} else if (executionDefinition.getContainerId().isPresent()) {
			this.container = executionDefinition.getContainerId().get();
		}
	}

	private void executePostCommands(Job executionDefinition) {
		String[] command = executionDefinition.getPostCommand().get()
				.toArray(new String[executionDefinition.getPostCommand().get().size()]);
		ExecCreation execCreation = prepareExecution(executionDefinition, command);
		executeCommand(execCreation);
	}

	private boolean executePreCommands(Job executionDefinition) {
		String[] command = executionDefinition.getPreCommand().get()
				.toArray(new String[executionDefinition.getPreCommand().get().size()]);
		ExecCreation execCreation = prepareExecution(executionDefinition, command);
		boolean result = false;
		executeCommand(execCreation);
		if (!timedOut && Integer.valueOf(0).equals(returnCode)) {
			result = true;
		}
		return result;
	}

	private boolean executeCommands(Job executionDefinition) {
		String[] command = executionDefinition.getCommand().get()
				.toArray(new String[executionDefinition.getCommand().get().size()]);
		ExecCreation execCreation = prepareExecution(executionDefinition, command);
		boolean result = false;
		executeCommand(execCreation);
		if (!timedOut && Integer.valueOf(0).equals(returnCode)) {
			result = true;
		}
		return result;
	}

	private ExecCreation prepareExecution(Job executionDefinition, String[] command) {
		List<ExecCreateParam> parameters = new ArrayList<>();
		parameters.add(DockerClient.ExecCreateParam.attachStderr());
		parameters.add(DockerClient.ExecCreateParam.attachStdout());
		if (executionDefinition.getWorkingDir().isPresent()) {
			parameters.add(new ExecCreateParam("WorkingDir", executionDefinition.getWorkingDir().get()));
		}
		ExecCreateParam[] executionParams = parameters.toArray(new ExecCreateParam[parameters.size()]);
		ExecCreation execCreation = null;
		try {
			execCreation = dockerClient.execCreate(this.container, command, executionParams);
		} catch (DockerException | InterruptedException e) {
			String message = String.format("Execution creation for job %s failed!",
					executionDefinition.getJobLabel());
			LOG.error(message, e);
			throw new ExecutorException(message, e);
		}
		return execCreation;
	}

	private void executeCommand(ExecCreation processBuilder) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<Void> task = new Callable<Void>() {
			public Void call() {
				LogStream output = null;
				try {
					try {
						output = dockerClient.execStart(processBuilder.id());
						output.attach(outStream, outErrorStream, false);
					} catch (Exception e) {
						// Problem with Unix socket and spotify docker-client library
						// Must ignore error. Happens around one of three executions
						if (!e.getMessage().contains("Connection reset by peer")) {
							throw e;
						} else {
							LOG.debug("Execution error ignored.", e);
						}
					}
					ExecState state = dockerClient.execInspect(processBuilder.id());
					RemoteContainer.this.returnCode = state.exitCode();
				} catch (DockerException | InterruptedException | IOException e) {
					String message = String.format("Error executing job %s !",
							RemoteContainer.this.jobDefinition.getJobLabel());
					LOG.error(message, e);
					throw new ExecutorException(message, e);
				} finally {
					if (output != null) {
						output.close();
					}
				}
				RemoteContainer.this.timedOut = false;
				return null;
			}
		};
		executeTask(executor, task);

	}

	private void executeTask(ExecutorService executor, Callable<Void> task) {
		Future<Void> future = executor.submit(task);
		if (this.jobDefinition.getTimeoutMinutes().isPresent()) {
			try {
				future.get(this.jobDefinition.getTimeoutMinutes().get(), TimeUnit.MINUTES);
			} catch (InterruptedException | ExecutionException e) {
				String message = String.format("Error executing job %s!",
						RemoteContainer.this.jobDefinition.getJobLabel());
				LOG.error(message, e);
				throw new ExecutorException(message, e);
			} catch (TimeoutException e) {
				LOG.error("Job timed out {}!", this.jobDefinition.getJobLabel());
				this.timedOut = true;
			}
		} else {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				String message = String.format("Error executing job %s!",
						this.jobDefinition.getJobLabel());
				LOG.error(message, e);

			}
		}
	}

	public Integer getReturnCode() {
		return this.returnCode;
	}

	public boolean isTimedOut() {
		return this.timedOut;
	}

	public OutputStream getOutStream() {
		return outStream;
	}

	public void setOutStream(OutputStream outStream) {
		this.outStream = outStream;
	}

	public OutputStream getOutErrorStream() {
		return outErrorStream;
	}

	public void setOutErrorStream(OutputStream outErrorStream) {
		this.outErrorStream = outErrorStream;
	}

}
