package com.blacklabelops.crow.console.executor.console;

import org.junit.rules.ExternalResource;

import com.blacklabelops.crow.console.definition.JobDefinition;
import com.blacklabelops.crow.console.executor.console.LocalConsole;

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
