package com.blacklabelops.crow.executor.docker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.executor.ExecutorException;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;

class RemoteContainer {
	
	public static final Logger LOG = LoggerFactory.getLogger(RemoteContainer.class);

    private File outputFile;

    private File outputErrorFile;
    
    private OutputStream outStream;
    
    private OutputStream outErrorStream;

    private Integer returnCode;
    
    private boolean timedOut;
    
    private DockerClient dockerClient = DockerClientBuilder.getInstance().build();
    
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
        prepareRedirects();
        boolean preResult = true;
        try {
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
    
    private void executePostCommands(JobDefinition executionDefinition) {
		ExecCreateCmd processBuilder = dockerClient
				.execCreateCmd(executionDefinition.getContainerName())
				.withAttachStderr(true)
				.withAttachStdout(true)
				.withCmd(executionDefinition.getPostCommand().toArray(new String[executionDefinition.getPostCommand().size()]));
	    if (executionDefinition.getTimeoutMinutes() != null) {
	    		executeCommandWithTimeout(processBuilder, executionDefinition.getTimeoutMinutes());
	    } else {
	    		executeCommand(processBuilder);
	    }
	}

	private boolean executePreCommands(JobDefinition executionDefinition) {
		ExecCreateCmd processBuilder = dockerClient
				.execCreateCmd(executionDefinition.getContainerName())
				.withAttachStderr(true)
				.withAttachStdout(true)
				.withCmd(executionDefinition.getPreCommand().toArray(new String[executionDefinition.getPreCommand().size()]));
	    	boolean result = false;
	    if (executionDefinition.getTimeoutMinutes() != null) {
	    		executeCommandWithTimeout(processBuilder, executionDefinition.getTimeoutMinutes());
	    } else {
	    		executeCommand(processBuilder);
	    }
	    if (!timedOut && Integer.valueOf(0).equals(returnCode)) {
	    		result = true;
	    }
	    return result; 
	}
    
    private boolean executeCommands(JobDefinition executionDefinition) {
    		ExecCreateCmd processBuilder = dockerClient
				.execCreateCmd(executionDefinition.getContainerName())
				.withAttachStderr(true)
				.withAttachStdout(true)
				.withCmd(executionDefinition.getCommand().toArray(new String[executionDefinition.getCommand().size()]));
    		boolean result = false;
        if (executionDefinition.getTimeoutMinutes() != null) {
        		executeCommandWithTimeout(processBuilder, executionDefinition.getTimeoutMinutes());
        } else {
        		executeCommand(processBuilder);
        }
        if (!timedOut && Integer.valueOf(0).equals(returnCode)) {
        		result = true;
        }
        return result; 
	}

    private void executeCommand(ExecCreateCmd processBuilder) {
    		ExecStartResultCallback callback = new ExecStartResultCallback(outStream, outErrorStream);
        ExecCreateCmdResponse response = processBuilder.exec();
        try {
			dockerClient
				.execStartCmd(response.getId())
				.exec(callback)
				.awaitCompletion().close();
		} catch (InterruptedException e) {
			String message = String.format("Docker execution interruped in container %s!", jobDefinition.getContainerName());
			LOG.error(message, e);
			throw new ExecutorException(message, e);
		} catch (IOException e) {
			String message = String.format("Docker execution in container %s could not close after finishing!", jobDefinition.getContainerName());
			LOG.error(message, e);
			throw new ExecutorException(message, e);
		}
        returnCode = Integer.valueOf(0);
    }
    
    private void executeCommandWithTimeout(ExecCreateCmd processBuilder, int minutes) {
    		ExecStartResultCallback callback = new ExecStartResultCallback(outStream, outErrorStream);
        ExecCreateCmdResponse response = processBuilder.exec();
        try {
			timedOut = dockerClient
					.execStartCmd(response.getId())
					.exec(callback)
					.awaitCompletion(minutes, TimeUnit.MINUTES);
			callback.close();
		} catch (InterruptedException e) {
			String message = String.format("Docker execution interruped in container %s!", jobDefinition.getContainerName());
			LOG.error(message, e);
			throw new ExecutorException(message, e);
		} catch (IOException e) {
			String message = String.format("Docker execution in container %s could not close after finishing!", jobDefinition.getContainerName());
			LOG.error(message, e);
			throw new ExecutorException(message, e);
		}
        returnCode = Integer.valueOf(0);
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
