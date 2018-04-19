package com.blacklabelops.crow.console.executor.console;

import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.util.FileAsserter;

public class ExecutorConsoleMassIT {

	public LocalConsole cli;

	public Job definition;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Rule
	public ExecutorConsoleRule executorRule = new ExecutorConsoleRule();

	@Rule
	public FileAsserter outputFile = new FileAsserter();

	@Rule
	public FileAsserter errorFile = new FileAsserter();

	@Before
	public void setup() {
		assert !System.getProperty("os.name").startsWith("Windows");
		cli = executorRule.getExecutor();
		definition = executorRule.getDefinition();
		cli.setOutputFile(outputFile.getFile());
		cli.setOutputErrorFile(errorFile.getFile());
	}

	@After
	public void tearDown() {
		cli = null;
		definition = null;
	}

	@Test
	public void whenTenCharsThenCorrectOutputfilesize() {
		int size = 10;
		String print = createPrintCommand(size);
		String[] command = new String[] { "/bin/bash", "-c", print };
		definition = definition.withCommand(Arrays.asList(command));
		cli.execute(definition);
		Assert.assertEquals(size, outputFile.getFilesize());
	}

	private String createPrintCommand(int size) {
		String times = "{1.." + size + "}";
		return "printf '=%.0s' " + times;
	}

	@Test
	public void whenMillionharsThenCorrectOutputfilesize() {
		int size = 1000000;
		String print = createPrintCommand(size);
		String[] command = new String[] { "/bin/bash", "-c", print };
		definition = definition.withCommand(Arrays.asList(command));
		cli.execute(definition);
		Assert.assertEquals(size, outputFile.getFilesize());
	}

}
