package com.blacklabelops.crow.console.executor.docker;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.blacklabelops.crow.console.definition.Job;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

public class ContainerRemoteIT {

	public RemoteContainer remoteContainer;

	public Job definition;

	public static DockerTestContainerFactory containerFactory;

	public static DockerClient dockerClient;

	public ByteArrayOutputStream output;

	public ByteArrayOutputStream errorOutput;

	@Before
	public void setup() {
		assert !System.getProperty("os.name").startsWith("Windows");
		remoteContainer = new RemoteContainer();
		output = new ByteArrayOutputStream();
		errorOutput = new ByteArrayOutputStream();
		remoteContainer.setOutStream(output);
		remoteContainer.setOutErrorStream(errorOutput);
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
	@Ignore
	public void testRun_CommandTakesLongerThanTimeOut_JobTimedOut() throws DockerException, InterruptedException {
		String containerId = containerFactory.runContainer();
		Job jobDefinition = Job.builder().name("A").id("A").command(Arrays.asList("sleep", "80000")).timeoutMinutes(1)
				.containerId(containerId).build();

		remoteContainer.execute(jobDefinition);

		assertTrue(remoteContainer.isTimedOut());
	}
}
