package com.blacklabelops.crow.docker.client.spotify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.docker.client.spotify.test.DockerTestClientFactory;
import com.blacklabelops.crow.docker.client.spotify.test.SpotifyDockerTestClient;
import com.blacklabelops.crow.docker.client.test.IDockerClientTest;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ExecCreateParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.ExecState;
import com.spotify.docker.client.messages.Info;

public class DockerSmokeIT {

	private Logger LOG = LoggerFactory.getLogger(DockerSmokeIT.class);	

	public int numberOfContainers = 5;

	public List<String> containers = new ArrayList<>(numberOfContainers);

	public DockerClient dockerClient;
	
	public IDockerClientTest dockerTestClient;

	@Before
	public void setupClass() throws DockerCertificateException, DockerException, InterruptedException {		
		dockerTestClient = DockerTestClientFactory.initializeDockerClient();
		dockerClient = ((SpotifyDockerTestClient) dockerTestClient).getDockerClient();
		dockerTestClient.runSomeContainers(numberOfContainers);
	}

	@After
	public void tearDownClass() {
		dockerTestClient.deleteContainers();
	}

	@Test
	public void testInfo_AccessingInfo_InfoAccessible() throws DockerException, InterruptedException {
		Info info = dockerClient.info();

		assertNotNull(info);
	}

	@Test
	public void testListContainers_ContainersStarted_ContainersCanBeFound() throws DockerException,
			InterruptedException {
		List<Container> containers = dockerClient.listContainers();
		List<String> startedContainers = dockerTestClient.getContainerIds();

		assertTrue(assertContainersFound(startedContainers, containers));
	}

	private boolean assertContainersFound(List<String> startedContainers, List<Container> containers) {
		boolean found = true;
		for (String startedContainer : startedContainers) {
			if (!containers.stream().filter(c -> startedContainer.equals(c.id())).findFirst().isPresent()) {
				found = false;
				break;
			}
		}
		return found;
	}

	@Test
	public void testInspectContainers_SequentialInspection() {
		List<String> startedContainers = dockerTestClient.getContainerIds();

		long startTime = System.currentTimeMillis();
		List<ContainerInfo> inspections = startedContainers.stream().map(c -> {
			try {
				return dockerClient.inspectContainer(c);
			} catch (DockerException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());
		long endTime = System.currentTimeMillis();
		LOG.info("Execution time {}", endTime - startTime);

		assertEquals(startedContainers.size(), inspections.size());
	}

	@Test
	public void testInspectContainers_ParallelInspection() {
		List<String> startedContainers = dockerTestClient.getContainerIds();

		long startTime = System.currentTimeMillis();
		List<ContainerInfo> inspections = startedContainers.parallelStream().map(c -> {
			try {
				return dockerClient.inspectContainer(c);
			} catch (DockerException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());
		long endTime = System.currentTimeMillis();
		LOG.info("Execution time {}", endTime - startTime);

		assertEquals(startedContainers.size(), inspections.size());
	}

	@Test
	public void testExecution_ExecuteCommandInContainer_OutputExecuted() throws InterruptedException,
			IOException, DockerException {
		String[] command = new String[] { "echo", "HelloWorld" };
		String container = dockerTestClient.getContainerIds().stream().findFirst().get();
		ExecCreation execCreation = dockerClient.execCreate(container, command, DockerClient.ExecCreateParam
				.attachStdout(),
				DockerClient.ExecCreateParam.attachStderr());

		LogStream output = dockerClient.execStart(execCreation.id());
		String execOutput = output.readFully();
		ExecState state = dockerClient.execInspect(execCreation.id());

		LOG.info("Output: {}", execOutput);

		assertEquals("HelloWorld\n", execOutput);
		assertEquals(Integer.valueOf(0), state.exitCode());
	}

	/**
	 * Does not work on circleci (api version 1.32 available, 1.35 required)
	 */
	@Test
	@Ignore
	public void testExecution_ConfigWithWorkingDir_CommandExecutedInWorkingDir() throws InterruptedException,
			IOException, DockerException {
		String[] command = new String[] { "pwd" };
		String container = dockerTestClient.getContainerIds().stream().findFirst().get();
		ExecCreation execCreation = dockerClient.execCreate(container, command, DockerClient.ExecCreateParam
				.attachStdout(),
				DockerClient.ExecCreateParam.attachStderr(), new ExecCreateParam("WorkingDir", "/var/www"));

		LogStream output = dockerClient.execStart(execCreation.id());
		String execOutput = output.readFully();
		ExecState state = dockerClient.execInspect(execCreation.id());

		LOG.info("Output: {}", execOutput);

		assertEquals("/var/www\n", execOutput);
		assertEquals(Integer.valueOf(0), state.exitCode());
	}
}
