package com.blacklabelops.crow.executor.console;

import org.junit.rules.ExternalResource;

import com.blacklabelops.crow.definition.JobDefinition;

public class ExecutorConsoleRule extends ExternalResource {

    public LocalConsole executor;

    public JobDefinition definition;

    public ExecutorConsoleRule() {
        super();
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        executor = new LocalConsole();
        definition = new JobDefinition();
    }

    @Override
    protected void after() {
        super.after();
        executor = null;
        definition = null;
    }

    public LocalConsole getExecutor() {
        return executor;
    }

    public JobDefinition getDefinition() {
        return definition;
    }
}
