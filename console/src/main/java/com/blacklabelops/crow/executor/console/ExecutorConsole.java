package com.blacklabelops.crow.executor.console;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.blacklabelops.crow.definition.IJobDefinition;

public class ExecutorConsole {

    private File inputFile;

    private File outputFile;

    private File outputErrorFile;

    private Integer returnCode;
    
    private boolean timedOut;

    public ExecutorConsole() {
        super();
    }

    private void checkDefinition(IJobDefinition executionDefinition) {
        if (executionDefinition == null) throw new ExecutorException("Executor has no job definition!");
        if (executionDefinition.getCommand() == null) throw new ExecutorException("Executor has no command specified");
    }

    public void execute(IJobDefinition executionDefinition) {
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

    private void executePostCommands(IJobDefinition executionDefinition) {
		ProcessBuilder processBuilder = new ProcessBuilder();
	    processBuilder.command(executionDefinition.getPostCommand());
	    extendEnvironmentVariables(processBuilder,executionDefinition);
	    processBuilder.directory(executionDefinition.getWorkingDir());
	    prepareRedirects(processBuilder);
	    if (executionDefinition.getTimeoutMinutes() != null) {
	    		executeCommandWithTimeout(processBuilder, executionDefinition.getTimeoutMinutes());
	    } else {
	    		executeCommand(processBuilder);
	    }
	}

	private boolean executePreCommands(IJobDefinition executionDefinition) {
	    	boolean result = false;
		ProcessBuilder processBuilder = new ProcessBuilder();
	    processBuilder.command(executionDefinition.getPreCommand());
	    extendEnvironmentVariables(processBuilder,executionDefinition);
	    processBuilder.directory(executionDefinition.getWorkingDir());
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
    
    private boolean executeCommands(IJobDefinition executionDefinition) {
    		boolean result = false;
    		ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(takeOverCommands(executionDefinition));
        extendEnvironmentVariables(processBuilder,executionDefinition);
        processBuilder.directory(executionDefinition.getWorkingDir());
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

	private List<String> takeOverCommands(IJobDefinition executionDefinition) {
        return executionDefinition.getCommand();
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

    private void extendEnvironmentVariables(ProcessBuilder processBuilder, IJobDefinition executionDefinition) {
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
