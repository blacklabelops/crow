package com.blacklabelops.crow.executor.console;

import org.junit.rules.ExternalResource;

import com.blacklabelops.crow.definition.JobDefinition;

public class ExecutorConsoleRule extends ExternalResource {

    public ExecutorConsole executor;

    public JobDefinition definition;

    public ExecutorConsoleRule() {
        super();
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        executor = new ExecutorConsole();
        definition = new JobDefinition();
    }

    @Override
    protected void after() {
        super.after();
        executor = null;
        definition = null;
    }

    public ExecutorConsole getExecutor() {
        return executor;
    }

    public JobDefinition getDefinition() {
        return definition;
    }
}
