package com.blacklabelops.crow.executor.console;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.executor.ExecutorException;
import com.cronutils.utils.StringUtils;

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

    private void checkDefinition(JobDefinition executionDefinition) {
        if (executionDefinition == null) throw new ExecutorException("Executor has no job definition!");
        if (executionDefinition.getCommand() == null) throw new ExecutorException("Executor has no command specified");
    }

    public void execute(JobDefinition executionDefinition) {
        checkDefinition(executionDefinition);
        boolean preResult = true;
        if (executionDefinition.getPreCommand() != null && !executionDefinition.getPreCommand().isEmpty()) {
        		preResult = executePreCommands(executionDefinition);
        }
        boolean result = preResult ? executeCommands(executionDefinition) : preResult;
        	if (result && executionDefinition.getPostCommand() != null && !executionDefinition.getPostCommand().isEmpty()) {
        		executePostCommands(executionDefinition);
        	}        
    }

    private void executePostCommands(JobDefinition executionDefinition) {
		ProcessBuilder processBuilder = new ProcessBuilder();
	    processBuilder.command(executionDefinition.getPostCommand());
	    extendEnvironmentVariables(processBuilder,executionDefinition);
	    setWorkingDirectory(executionDefinition, processBuilder);
	    prepareRedirects(processBuilder);
	    if (executionDefinition.getTimeoutMinutes() != null) {
	    		executeCommandWithTimeout(processBuilder, executionDefinition.getTimeoutMinutes());
	    } else {
	    		executeCommand(processBuilder);
	    }
	}

	private void setWorkingDirectory(JobDefinition executionDefinition, ProcessBuilder processBuilder) {
		if (!StringUtils.isEmpty(executionDefinition.getWorkingDir())) {
			File workingDirectory = new File(executionDefinition.getWorkingDir());
			if (workingDirectory.exists() && workingDirectory.isDirectory()) {
				processBuilder.directory(workingDirectory);
			}
		}
	}

	private boolean executePreCommands(JobDefinition executionDefinition) {
	    	boolean result = false;
		ProcessBuilder processBuilder = new ProcessBuilder();
	    processBuilder.command(executionDefinition.getPreCommand());
	    extendEnvironmentVariables(processBuilder,executionDefinition);
	    setWorkingDirectory(executionDefinition, processBuilder);
	    prepareRedirects(processBuilder);
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
    		boolean result = false;
    		ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(takeOverCommands(executionDefinition));
        extendEnvironmentVariables(processBuilder,executionDefinition);
        setWorkingDirectory(executionDefinition, processBuilder);
        prepareRedirects(processBuilder);
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

	private List<String> takeOverCommands(JobDefinition executionDefinition) {
		List<String> command = executionDefinition.getCommand();
		LOG.debug("Executing command: {}", command);
        return command;
    }

    private void executeCommand(ProcessBuilder processBuilder) {
        Process process = null;
        try {
            process = processBuilder.start();
            returnCode = process.waitFor();
        } catch (IOException |InterruptedException e) {
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
        } catch (IOException |InterruptedException e) {
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

    private void extendEnvironmentVariables(ProcessBuilder processBuilder, JobDefinition executionDefinition) {
        Map<String, String> environmentVariables = processBuilder.environment();
        if (executionDefinition.getEnvironmentVariables() != null && !executionDefinition.getEnvironmentVariables().isEmpty()) {
            environmentVariables.putAll(executionDefinition.getEnvironmentVariables());
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
