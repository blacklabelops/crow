package com.blacklabelops.crow.console.executor.docker;

import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.executor.ExecutorException;
import com.blacklabelops.crow.docker.client.DockerClientException;
import com.blacklabelops.crow.docker.client.DockerJob;
import com.blacklabelops.crow.docker.client.ExecuteCommandResult;
import com.blacklabelops.crow.docker.client.IDockerClient;
import com.blacklabelops.crow.docker.client.spotify.DockerClientFactory;

class RemoteContainer {

	public static final Logger LOG = LoggerFactory.getLogger(RemoteContainer.class);

	private OutputStream outStream;

	private OutputStream outErrorStream;

	private Integer returnCode;

	private boolean timedOut;

	private IDockerClient dockerClient;

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
		try {
			dockerClient = DockerClientFactory.initializeDockerClient();
		} catch (DockerClientException e) {
			throw new ExecutorException(e);
		}
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
	
	private DockerJob buildDockerJob(Job executionDefinition) {		
		return DockerJob.builder()
				.output(this.outStream)
				.errorOutput(this.outErrorStream)
				.workingDir(executionDefinition.getWorkingDir())
				.jobLabel(executionDefinition.getJobLabel())
				.containerId(this.container)
				.build();
	}
	
	private void executePostCommands(Job executionDefinition) {
		String[] command = executionDefinition.getPostCommand().get()
				.toArray(new String[executionDefinition.getPostCommand().get().size()]);
		DockerJob dockerJob = buildDockerJob(executionDefinition);
		Callable<ExecuteCommandResult> execCreation = dockerClient.prepareExecuteCommand(dockerJob, command);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executeTask(executor, execCreation);
	}

	private boolean executePreCommands(Job executionDefinition) {
		String[] command = executionDefinition.getPreCommand().get()
				.toArray(new String[executionDefinition.getPreCommand().get().size()]);
		DockerJob dockerJob = buildDockerJob(executionDefinition);
		Callable<ExecuteCommandResult> execCreation = dockerClient.prepareExecuteCommand(dockerJob, command);
		boolean result = false;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executeTask(executor, execCreation);
		if (!timedOut && Integer.valueOf(0).equals(returnCode)) {
			result = true;
		}
		return result;
	}	

	private boolean executeCommands(Job executionDefinition) {
		String[] command = executionDefinition.getCommand().get()
				.toArray(new String[executionDefinition.getCommand().get().size()]);
		DockerJob dockerJob = buildDockerJob(executionDefinition);
		Callable<ExecuteCommandResult> execCreation = dockerClient.prepareExecuteCommand(dockerJob, command);
		boolean result = false;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executeTask(executor, execCreation);
		if (!timedOut && Integer.valueOf(0).equals(returnCode)) {
			result = true;
		}
		return result;
	}	

	private void executeTask(ExecutorService executor, Callable<ExecuteCommandResult> task) {
		Future<ExecuteCommandResult> future = executor.submit(task);
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
				ExecuteCommandResult result = future.get();
				result.getReturnCode().ifPresent(returnCode -> this.returnCode = returnCode);
				this.timedOut = result.isTimedOut();
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
