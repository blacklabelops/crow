package com.blacklabelops.crow.console.executor.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.executor.ExecutorException;
import com.blacklabelops.crow.console.util.FileAsserter;

public class ExecutorConsoleUnixIntegrationTest {

	public LocalConsole cli;

	public Job definition;

	public String newLine = System.getProperty("line.separator");

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Rule
	public ExecutorConsoleRule executorRule = new ExecutorConsoleRule();

	@Rule
	public FileAsserter inputFile = new FileAsserter();

	@Rule
	public FileAsserter outputFile = new FileAsserter();

	@Rule
	public FileAsserter errorFile = new FileAsserter();

	@Before
	public void setup() {
		assert !System.getProperty("os.name").startsWith("Windows");
		cli = executorRule.getExecutor();
		definition = executorRule.getDefinition();
		cli.setInputFile(inputFile.getFile());
		cli.setOutputFile(outputFile.getFile());
		cli.setOutputErrorFile(errorFile.getFile());
	}

	@After
	public void tearDown() {
		cli = null;
		definition = null;
	}

	@Test
	public void whenNoDefinitionThenException() {
		exception.expect(ExecutorException.class);
		cli.execute(null);
	}

	@Test
	public void whenNoCommandThenException() {
		exception.expect(ExecutorException.class);
		cli.execute(definition);
	}

	@Test
	public void whenEchoHelloThenHelloInOutput() {
		String outputString = "hello";
		definition = definition.withCommand(Arrays.asList("echo", outputString));
		cli.execute(definition);
		outputFile.assertContainsLine(outputString);
		Assert.assertEquals(outputString.length() + newLine.length(), outputFile.getFilesize());
	}

	@Test
	public void whenUnknownCommandThenOutputEmpty() {
		exception.expect(ExecutorException.class);
		definition = definition.withCommand(Arrays.asList("efrferfere"));
		cli.execute(definition);
		assertTrue(outputFile.isEmpty());
	}

	@Test
	public void whenUnknownCommandThenErrorfileWritten() {
		exception.expect(ExecutorException.class);
		definition = definition.withCommand(Arrays.asList("efrferfere"));
		cli.execute(definition);
		assertTrue(!errorFile.isEmpty());
	}

	@Test
	public void whenPrintCommandExistThenOutputEmpty() {
		definition = definition.withCommand(Arrays.asList("printf"));
		cli.execute(definition);
		assertTrue(outputFile.isEmpty());
	}

	@Test
	public void whenSimpleEchoThenOnlyNewlineOutput() {
		definition = definition.withCommand(Arrays.asList("echo"));
		cli.execute(definition);
		Assert.assertEquals(newLine.length(), outputFile.getFilesize());
		outputFile.assertEquals(newLine);
	}

	@Test
	public void whenEnvironmentVariableThenSeeItOnOutput() {
		Map<String, String> envvironmentVariable = new HashMap<>();
		envvironmentVariable.put("MY_TEST", "MYVALUE");
		definition = definition.withEnvironmentVariables(envvironmentVariable).withCommand(Arrays.asList("/bin/sh",
				"-c", "echo $MY_TEST"));
		cli.execute(definition);
		outputFile.assertContainsLine("MYVALUE");
	}

	@Test
	public void whenCommandOkayThenZeroReturnCode() {
		definition = definition.withCommand(Arrays.asList("echo"));
		cli.execute(definition);
		assertNotNull("Must have return code!", cli.getReturnCode());
		assertEquals("Must be return code for okay!", 0, cli.getReturnCode().intValue());
	}

	@Test
	public void whenCommandErrorThenReturnNonZeroCode() {
		definition = definition.withCommand(Arrays.asList("/bin/sh", "-c", "exit 42"));
		cli.execute(definition);
		assertNotNull("Must have return code!", cli.getReturnCode());
		assertEquals("Must be expected return code!", 42, cli.getReturnCode().intValue());
	}

	@Test
	public void whenEchoCommandAsListThenEmptyOutput() {
		List<String> command = new ArrayList<>();
		command.add("echo");
		definition = definition.withCommand(command);
		cli.execute(definition);
		outputFile.assertEquals(newLine);
	}

	@Test
	public void whenEchoCommandAsListThenNonZeroReturnCode() {
		List<String> command = new ArrayList<>();
		command.add("echo");
		definition = definition.withCommand(command);
		cli.execute(definition);
		assertNotNull("Must have return code!", cli.getReturnCode());
		assertEquals("Must be return code for okay!", 0, cli.getReturnCode().intValue());
	}

}
