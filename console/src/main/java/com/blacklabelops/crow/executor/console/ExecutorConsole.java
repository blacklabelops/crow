package com.blacklabelops.crow.executor.console;

import java.io.*;
import java.util.Map;

/**
 * Created by steffenbleul on 19.12.16.
 */
public class ExecutorConsole {

    private File inputFile;

    private File outputFile;

    private File outputErrorFile;

    private Integer returnCode;

    public ExecutorConsole() {
        super();
    }

    private void checkDefinition(DefinitionConsole executionDefinition) {
        if (executionDefinition == null) throw new ExecutorException("Executor has no job definition!");
        if (executionDefinition.getCommand() == null) throw new ExecutorException("Executor has no command specified");
    }

    public void execute(DefinitionConsole executionDefinition) {
        checkDefinition(executionDefinition);
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(executionDefinition.getCommand());
        extendEnvironmentVariables(processBuilder,executionDefinition);
        prepareRedirects(processBuilder);
        executeCommand(processBuilder);
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

    private void prepareRedirects(ProcessBuilder processBuilder) {
        if (outputFile != null) {
            processBuilder.redirectOutput(outputFile);
        }
        if (outputErrorFile != null) {
            processBuilder.redirectError(outputErrorFile);
        }
        if (inputFile != null) {
            processBuilder.redirectInput(inputFile);
        }
    }

    private void extendEnvironmentVariables(ProcessBuilder processBuilder, DefinitionConsole executionDefinition) {
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
        return returnCode;
    }
}
