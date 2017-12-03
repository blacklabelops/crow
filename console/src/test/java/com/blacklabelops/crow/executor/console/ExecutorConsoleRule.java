package com.blacklabelops.crow.executor.console;

import org.junit.rules.ExternalResource;

/**
 * Created by steffenbleul on 20.12.16.
 */
public class ExecutorConsoleRule extends ExternalResource {

    public ExecutorConsole executor;

    public DefinitionConsole definition;

    public ExecutorConsoleRule() {
        super();
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        executor = new ExecutorConsole();
        definition = new DefinitionConsole();
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

    public DefinitionConsole getDefinition() {
        return definition;
    }
}
