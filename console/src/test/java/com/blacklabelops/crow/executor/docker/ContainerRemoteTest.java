package com.blacklabelops.crow.executor.docker;

import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blacklabelops.crow.definition.JobDefinition;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

public class ContainerRemoteTest {

	public RemoteContainer cli;

	public JobDefinition definition;

	public static DockerTestContainerFactory containerFactory;

	public static DockerClient dockerClient;

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
		containerFactory = new DockerTestContainerFactory(dockerClient);
	}

	@AfterClass
	public static void tearDownClass() {
		containerFactory.deleteContainers();
	}

	@Test
	public void testRun_ExecuteCommandInContainer_ResultInFile() throws DockerException, InterruptedException {
		String containerId = containerFactory.runContainer();
		JobDefinition jobDefinition = new JobDefinition();
		jobDefinition.setJobName("A");
		jobDefinition.setCommand("echo", "HelloWorld");
		jobDefinition.setContainerId(containerId);

		cli.execute(jobDefinition);

		String message = new String(output.toByteArray());

		assertThat(message, CoreMatchers.containsString("HelloWorld"));
	}

	@Test
	public void testRun_ExecuteErrorCommandInContainer_ResultInErrorFile()
			throws DockerException, InterruptedException {
		String containerId = containerFactory.runContainer();
		JobDefinition jobDefinition = new JobDefinition();
		jobDefinition.setJobName("A");
		jobDefinition.setCommand("sh", "-c", ">&2 echo error");
		jobDefinition.setContainerId(containerId);

		cli.execute(jobDefinition);

		String message = new String(errorOutput.toByteArray());

		assertThat(message, CoreMatchers.containsString("error"));
	}

	@Test
	public void testRun_FaultyCommand_ReturnCodeNotZero() throws DockerException, InterruptedException {
		String containerId = containerFactory.runContainer();
		JobDefinition jobDefinition = new JobDefinition();
		jobDefinition.setJobName("A");
		jobDefinition.setCommand("bash", "-c", "echo HelloWorld");
		jobDefinition.setContainerId(containerId);

		cli.execute(jobDefinition);

		assertNotEquals(Integer.valueOf(0), cli.getReturnCode());
	}

	@Test
	public void testRun_ExecuteAllCommands_AllOutputsInFile() throws DockerException, InterruptedException {
		String containerId = containerFactory.runContainer();
		JobDefinition jobDefinition = new JobDefinition();
		jobDefinition.setJobName("A");
		jobDefinition.setCommand("echo", "command");
		jobDefinition.setPreCommand("echo", "preCommand");
		jobDefinition.setPostCommand("echo", "postCommand");
		jobDefinition.setContainerId(containerId);

		cli.execute(jobDefinition);

		String message = new String(output.toByteArray());

		assertThat(message, allOf(
				CoreMatchers.containsString("command"),
				CoreMatchers.containsString("preCommand"),
				CoreMatchers.containsString("postCommand")));
	}

}
