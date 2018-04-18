package com.blacklabelops.crow.console.executor.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.blacklabelops.crow.console.definition.JobDefinition;
import com.blacklabelops.crow.console.executor.console.ConsoleExecutor;
import com.blacklabelops.crow.console.logger.JobLogLogger;
import com.blacklabelops.crow.console.logger.TestableJobLogger;

public class SimpleConsoleUnixIntegrationIT {

	public ConsoleExecutor simpleConsole;

	public JobDefinition jobDefinition;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Before
	public void setup() {
		assert !System.getProperty("os.name").startsWith("Windows");
		jobDefinition = new JobDefinition();
	}

	@After
	public void tearDown() {
		jobDefinition = null;
	}

	@Test
	public void whenNoDefinitionThenNullpointerException() {
		exception.expect(NullPointerException.class);
		simpleConsole = new ConsoleExecutor(null, null, null);
		simpleConsole.run();
	}

	@Test
	public void whenEchoConsoleThenHelloOnLogs() {
		jobDefinition.setCommand("echo", "hello world");
		jobDefinition.setJobName("echoJob");
		TestableJobLogger logger = new TestableJobLogger();
		simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(logger).collect(
				Collectors.toList()));

		simpleConsole.run();

		String output = logger.getOutput().toString();
		assertThat("Message must match echo directive message!", output, CoreMatchers.containsString("hello world"));
	}

	@Test
	public void whenEchoErrorConsoleThenErrorOnLogs() {
		jobDefinition.setCommand("/bin/bash", "-c", ">&2 echo error");
		jobDefinition.setJobName("errorJob");
		TestableJobLogger logger = new TestableJobLogger();
		simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(logger).collect(
				Collectors.toList()));

		simpleConsole.run();

		String output = logger.getOutputError().toString();
		assertThat("Message must match echo directive message!", output, CoreMatchers.containsString("error"));
	}

	@Test(timeout = 120000)
	public void testRun_WhenTimeOutDefined_ProcessTimedout() {
		jobDefinition.setCommand("sleep", "200");
		jobDefinition.setTimeoutMinutes(1);
		jobDefinition.setJobName("echoJob");
		simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(new JobLogLogger("echoJob")).collect(
				Collectors.toList()));
		simpleConsole.run();
		assertTrue(simpleConsole.getExecutionResult().isTimedOut());
	}

	@Test(timeout = 5000)
	public void testRun_WhenTimeOutDefinedShortJob_JobIsNotTimedOut() {
		jobDefinition.setCommand("sleep", "2");
		jobDefinition.setTimeoutMinutes(1);
		jobDefinition.setJobName("echoJob");
		simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(new JobLogLogger("echoJob")).collect(
				Collectors.toList()));
		simpleConsole.run();
		assertFalse(simpleConsole.getExecutionResult().isTimedOut());
		assertEquals(Integer.valueOf(0), simpleConsole.getExecutionResult().getReturnCode());
	}

	@Test
	@Ignore
	public void testRun_WhenWorkingDirectoryIsDefined_ExecuteInWorkDirectory() {
		String tempDirectory = System.getProperty("java.io.tmpdir");
		jobDefinition.setCommand("pwd");
		jobDefinition.setJobName("workdirJob");
		jobDefinition.setWorkingDir(tempDirectory);
		simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(new JobLogLogger("workdirJob")).collect(
				Collectors.toList()));
		simpleConsole.run();
	}

	@Test
	@Ignore
	public void testRun_WhenPreCommandDefined_ExecutePreCommandsAndCommands() {
		jobDefinition.setCommand("echo", "hello world");
		jobDefinition.setPreCommand("echo", "hello preCommand");
		jobDefinition.setPostCommand("echo", "hello postCommand");
		simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(new JobLogLogger("echoJob")).collect(
				Collectors.toList()));
		simpleConsole.run();
	}
}