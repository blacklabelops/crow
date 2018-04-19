package com.blacklabelops.crow.console.executor.console;

import org.junit.rules.ExternalResource;

import com.blacklabelops.crow.console.definition.Job;

public class ExecutorConsoleRule extends ExternalResource {

	public LocalConsole executor;

	public Job definition;

	public ExecutorConsoleRule() {
		super();
	}

	@Override
	protected void before() throws Throwable {
		super.before();
		executor = new LocalConsole();
		definition = Job.builder().id("id").name("A").build();
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

	public Job getDefinition() {
		return definition;
	}
}
