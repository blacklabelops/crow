package com.blacklabelops.crow.console.executor.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
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

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.definition.JobId;
import com.blacklabelops.crow.console.logger.JobLogLogger;
import com.blacklabelops.crow.console.logger.TestableJobLogger;

public class SimpleConsoleUnixIntegrationIT {

	public ConsoleExecutor simpleConsole;

	public Job jobDefinition;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Before
	public void setup() {
		assert !System.getProperty("os.name").startsWith("Windows");
		jobDefinition = Job.of(JobId.of("id"), "name");
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
		jobDefinition = jobDefinition.withCommand(Arrays.asList("echo", "hello world")).withName("echoJob");
		TestableJobLogger logger = new TestableJobLogger();
		simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(logger).collect(
				Collectors.toList()));

		simpleConsole.run();

		String output = logger.getOutput().toString();
		assertThat("Message must match echo directive message!", output, CoreMatchers.containsString("hello world"));
	}

	@Test
	public void whenEchoErrorConsoleThenErrorOnLogs() {
		jobDefinition = jobDefinition.withCommand(Arrays.asList("/bin/bash", "-c", ">&2 echo error")).withName(
				"errorJob");
		TestableJobLogger logger = new TestableJobLogger();
		simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(logger).collect(
				Collectors.toList()));

		simpleConsole.run();

		String output = logger.getOutputError().toString();
		assertThat("Message must match echo directive message!", output, CoreMatchers.containsString("error"));
	}

	@Test(timeout = 120000)
	public void testRun_WhenTimeOutDefined_ProcessTimedout() {
		jobDefinition = jobDefinition.withCommand(Arrays.asList("sleep", "200")).withTimeoutMinutes(1).withName(
				"echoJob");
		simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(new JobLogLogger("echoJob")).collect(
				Collectors.toList()));
		simpleConsole.run();
		assertTrue(simpleConsole.getExecutionResult().isTimedOut());
	}

	@Test(timeout = 5000)
	public void testRun_WhenTimeOutDefinedShortJob_JobIsNotTimedOut() {
		jobDefinition = jobDefinition.withCommand(Arrays.asList("sleep", "2")).withTimeoutMinutes(1).withName(
				"echoJob");
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
		jobDefinition = jobDefinition.withCommand(Arrays.asList("pwd")).withWorkingDir(tempDirectory).withName(
				"workdirJob");
		simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(new JobLogLogger("workdirJob")).collect(
				Collectors.toList()));
		simpleConsole.run();
	}

	@Test
	@Ignore
	public void testRun_WhenPreCommandDefined_ExecutePreCommandsAndCommands() {
		jobDefinition = jobDefinition.withCommand(Arrays.asList("echo", "hello world")) //
				.withPreCommand(Arrays.asList("echo", "hello preCommand")) //
				.withPostCommand(Arrays.asList("echo", "hello postCommand"))
				.withName("echoJob");
		simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(new JobLogLogger("echoJob")).collect(
				Collectors.toList()));
		simpleConsole.run();
	}
}