package com.blacklabelops.crow.executor.docker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.executor.ExecutorException;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DefaultDockerClient.Builder;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ExecCreateParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.ExecState;

class RemoteContainer {
	
	public static final Logger LOG = LoggerFactory.getLogger(RemoteContainer.class);

    private File outputFile;

    private File outputErrorFile;
    
    private OutputStream outStream;
    
    private OutputStream outErrorStream;

    private Integer returnCode;
    
    private boolean timedOut;
    
    private DockerClient dockerClient;
    
    private JobDefinition jobDefinition;

    public RemoteContainer() {
        super();
    }
    
    private void checkDefinition(JobDefinition executionDefinition) {
        if (executionDefinition == null) throw new ExecutorException("Executor has no job definition!");
        if (executionDefinition.getCommand() == null) throw new ExecutorException("Executor has no command specified");
        jobDefinition = new JobDefinition(executionDefinition);
    }

    public void execute(JobDefinition executionDefinition) {
        checkDefinition(executionDefinition);
        initializeDockerClient(executionDefinition);
        boolean preResult = true;
        try {
        		prepareRedirects();
	        	if (executionDefinition.getPreCommand() != null && !executionDefinition.getPreCommand().isEmpty()) {
	        		preResult = executePreCommands(executionDefinition);
	        }
	        boolean result = preResult ? executeCommands(executionDefinition) : preResult;
	        	if (result && executionDefinition.getPostCommand() != null && !executionDefinition.getPostCommand().isEmpty()) {
	        		executePostCommands(executionDefinition);
	        	}   
        } finally {
			IOUtils.closeQuietly(outStream);
			IOUtils.closeQuietly(outErrorStream);
        }
    }
    
    private void initializeDockerClient(JobDefinition executionDefinition) {
    		try {
    			Builder builder = DefaultDockerClient.fromEnv();
    			if (!builder.uri().getScheme().equals("https") && builder.dockerCertificates() != null) {
    				builder.dockerCertificates(null);
    			}
    			dockerClient = builder.build();
		} catch (DockerCertificateException e) {
			String message = String.format("Unable to initialize Docker Client: Job %s, Container %s", executionDefinition.getJobName(), executionDefinition.getContainerName());
			LOG.error(message, e);
			throw new ExecutorException(message, e);
		}
	}

	private void executePostCommands(JobDefinition executionDefinition) {
		String[] command = executionDefinition.getPostCommand().toArray(new String[executionDefinition.getPostCommand().size()]);
		ExecCreation execCreation = prepareExecution(executionDefinition, command);
	    	executeCommand(execCreation);
	}

	private boolean executePreCommands(JobDefinition executionDefinition) {
		String[] command = executionDefinition.getPreCommand().toArray(new String[executionDefinition.getPreCommand().size()]);
		ExecCreation execCreation = prepareExecution(executionDefinition, command);
	    	boolean result = false;	    
	    	executeCommand(execCreation);
	    if (!timedOut && Integer.valueOf(0).equals(returnCode)) {
	    		result = true;
	    }
	    return result; 
	}
    
    private boolean executeCommands(JobDefinition executionDefinition) {
    		String[] command = executionDefinition.getCommand().toArray(new String[executionDefinition.getCommand().size()]);
    		ExecCreation execCreation = prepareExecution(executionDefinition, command);
    		boolean result = false;
        	executeCommand(execCreation);
        if (!timedOut && Integer.valueOf(0).equals(returnCode)) {
        		result = true;
        }
        return result; 
	}

	private ExecCreation prepareExecution(JobDefinition executionDefinition, String[] command) {
		List<ExecCreateParam> parameters = new ArrayList<>();
		parameters.add(DockerClient.ExecCreateParam.attachStderr());
		parameters.add(DockerClient.ExecCreateParam.attachStdout());
		if (!StringUtils.isEmpty(executionDefinition.getWorkingDir())) {
			parameters.add(new ExecCreateParam("WorkingDir", executionDefinition.getWorkingDir()));
		}
		ExecCreateParam[] executionParams = parameters.toArray(new ExecCreateParam[parameters.size()]);
		ExecCreation execCreation = null;
		try {
			execCreation = dockerClient.execCreate(executionDefinition.getContainerName(), command, executionParams);
		} catch (DockerException | InterruptedException e) {
			String message = String.format("Execution creation for job %s in container %s failed!", executionDefinition.getJobName(), executionDefinition.getContainerName());
			LOG.error(message,e);
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
					output = dockerClient.execStart(processBuilder.id());
					output.attach(outStream, outErrorStream, false);
					ExecState state = dockerClient.execInspect(processBuilder.id());
					RemoteContainer.this.returnCode = state.exitCode();
				} catch (DockerException | InterruptedException | IOException e) {
					String message = String.format("Error executing job %s in container %s", RemoteContainer.this.jobDefinition.getJobName(), RemoteContainer.this.jobDefinition.getContainerName());
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
		if (this.jobDefinition.getTimeoutMinutes() != null) {
			try {
				future.get(this.jobDefinition.getTimeoutMinutes(), TimeUnit.MINUTES);
			} catch (InterruptedException | ExecutionException e) {
				String message = String.format("Error executing job %s in container %s", RemoteContainer.this.jobDefinition.getJobName(), RemoteContainer.this.jobDefinition.getContainerName());
				LOG.error(message, e);
				throw new ExecutorException(message, e);
			} catch (TimeoutException e) {
				LOG.error("Job {} in container {} timed out!", this.jobDefinition.getJobName(), this.jobDefinition.getContainerName());
				this.timedOut = true;
			}
		} else {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				String message = String.format("Error executing job %s in container %s", this.jobDefinition.getJobName(), this.jobDefinition.getContainerName());
				LOG.error(message, e);
				throw new ExecutorException(message, e);
			}
		}
	}

    private void prepareRedirects() {
    		outStream = createOutputStream(outputFile);
    		outErrorStream =  createOutputStream(outputErrorFile);
    }

	private OutputStream createOutputStream(File outfile) {
		BufferedOutputStream outStream = null;
		try {
			outStream = new BufferedOutputStream(new FileOutputStream(outfile));
		} catch (FileNotFoundException e) {
			String message = String.format("Could not find output file %s", outfile);
			LOG.error(message, e);
			throw new ExecutorException(message, e);
		}
		return outStream;
	}
    
    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public void setOutputErrorFile(File outputErrorFile) {
        this.outputErrorFile = outputErrorFile;
    }

    public Integer getReturnCode() {
        return this.returnCode;
    }

	public boolean isTimedOut() {
		return this.timedOut;
	}
}
