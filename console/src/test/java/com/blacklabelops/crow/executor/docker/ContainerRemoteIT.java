package com.blacklabelops.crow.executor.docker;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blacklabelops.crow.definition.JobDefinition;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

public class ContainerRemoteIT {

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

	@Test(timeout = 120000)
	public void testRun_CommandTakesLongerThanTimeOut_JobTimedOut() throws DockerException, InterruptedException {
		String containerId = containerFactory.runContainer();
		JobDefinition jobDefinition = new JobDefinition();
		jobDefinition.setJobName("A");
		jobDefinition.setCommand("sleep", "90000");
		jobDefinition.setTimeoutMinutes(1);
		jobDefinition.setContainerId(containerId);

		cli.execute(jobDefinition);

		assertTrue(cli.isTimedOut());
	}
}
