package com.blacklabelops.crow.console.executor.docker;

import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.docker.client.IDockerClient;
import com.blacklabelops.crow.docker.client.spotify.DockerClientFactory;
import com.blacklabelops.crow.docker.client.spotify.test.DockerTestClientFactory;
import com.blacklabelops.crow.docker.client.test.IDockerClientTest;
import com.spotify.docker.client.exceptions.DockerException;

public class ContainerRemoteTestIT {

	public RemoteContainer cli;

	public Job definition;

	public static IDockerClientTest containerFactory;

	public static IDockerClient dockerClient;

	public ByteArrayOutputStream output;

	public ByteArrayOutputStream errorOutput;

	@Before
	public void setup() {
		assert !System.getProperty("os.name").startsWith("Windows");
		cli = new RemoteContainer();
		output = new ByteArrayOutputStream();
		errorOutput = new ByteArrayOutputStream();
		cli.setOutStream(output);
		cli.setOutErrorStream(errorOutput);
	}

	@BeforeClass
	public static void setupClass() throws InterruptedException, IOException {
		dockerClient = DockerClientFactory.initializeDockerClient();
		containerFactory = DockerTestClientFactory.initializeDockerClient();
	}

	@AfterClass
	public static void tearDownClass() {
		containerFactory.deleteContainers();
	}

	@Test
	public void testRun_ExecuteCommandInContainer_OutputCorrect() throws DockerException, InterruptedException {
		String containerId = containerFactory.runContainer();
		Job jobDefinition = Job.builder().name("A").id("A").command(Arrays.asList("echo", "HelloWorld")).containerId(
				containerId).build();

		cli.execute(jobDefinition);

		String message = new String(output.toByteArray());

		assertThat(message, CoreMatchers.containsString("HelloWorld"));
	}

	@Test
	public void testRun_ExecuteErrorCommandInContainer_ResultInErrorFile()
			throws DockerException, InterruptedException {
		String containerId = containerFactory.runContainer();
		Job jobDefinition = Job.builder().name("A").id("A").command(Arrays.asList("sh", "-c", ">&2 echo error"))
				.containerId(containerId).build();

		cli.execute(jobDefinition);

		String message = new String(errorOutput.toByteArray());

		assertThat(message, CoreMatchers.containsString("error"));
	}

	@Test
	public void testRun_FaultyCommand_ReturnCodeNotZero() throws DockerException, InterruptedException {
		String containerId = containerFactory.runContainer();
		Job jobDefinition = Job.builder().name("A").id("A").command(Arrays.asList("bash", "-c", "echo HelloWorld"))
				.containerId(containerId).build();

		cli.execute(jobDefinition);

		assertNotEquals(Integer.valueOf(0), cli.getReturnCode());
	}

	@Test
	public void testRun_ExecuteAllCommands_AllOutputsInFile() throws DockerException, InterruptedException {
		String containerId = containerFactory.runContainer();
		Job jobDefinition = Job.builder().id("A").name("A").command(Arrays.asList("echo", "command")).preCommand(Arrays
				.asList("echo", "preCommand"))
				.postCommand(Arrays.asList("echo", "postCommand")).containerId(containerId).build();

		cli.execute(jobDefinition);

		String message = new String(output.toByteArray());

		assertThat(message, allOf(
				CoreMatchers.containsString("command"),
				CoreMatchers.containsString("preCommand"),
				CoreMatchers.containsString("postCommand")));
	}

}
