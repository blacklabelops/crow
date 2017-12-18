package com.blacklabelops.crow.executor.console;

import com.blacklabelops.crow.executor.ExecutionMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by steffenbleul on 19.12.16.
 */
public class DefinitionConsole {

    private List<String> command;

    private Map<String, String> environmentVariables;

    private ExecutionMode executorMode = null;

    public DefinitionConsole() {
        super();
    }

    public List<String> getCommand() {
        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    public void setCommand(String... command) {
        this.command = new ArrayList<String>(Arrays.asList(command));
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public ExecutionMode getExecutionMode() {
        return executorMode;
    }

    public void setExecutionMode(ExecutionMode executorMode) {
        this.executorMode = executorMode;
    }
}
