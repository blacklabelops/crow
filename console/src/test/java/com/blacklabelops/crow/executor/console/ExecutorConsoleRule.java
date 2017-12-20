package com.blacklabelops.crow.executor.console;

import com.blacklabelops.crow.suite.FastTests;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExternalResource;

/**
 * Created by steffenbleul on 20.12.16.
 */
@Category(FastTests.class)
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
