package com.blacklabelops.crow.console.executor.docker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.ContainerRenameConflictException;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerRequestException;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.ExecState;

public class DockerSingleContainerSmokeTest {

	public static final String CONTAINER_NAME = "DockerSingleContainerSmokeTestContainer";

	private Logger LOG = LoggerFactory.getLogger(DockerSingleContainerSmokeTest.class);

	public DockerTestContainerFactory containerFactory;

	public List<String> containers = new LinkedList<>();

	public DockerClient dockerClient;

	@Before
	public void setupClass() throws DockerCertificateException, DockerException, InterruptedException {
		dockerClient = DockerClientFactory.initializeDockerClient();
		containerFactory = new DockerTestContainerFactory(dockerClient);
	}

	@After
	public void tearDownClass() {
		containerFactory.deleteContainers();
	}

	@Test
	public void testExecution_ExecuteInCommandNamedContainer_OutputExecuted() throws InterruptedException,
			IOException, DockerException {
		String[] command = new String[] { "echo", "HelloWorld" };
		containerFactory.runContainer(CONTAINER_NAME);
		ExecCreation execCreation = dockerClient.execCreate(CONTAINER_NAME, command, DockerClient.ExecCreateParam
				.attachStdout(),
				DockerClient.ExecCreateParam.attachStderr());
		LogStream output = dockerClient.execStart(execCreation.id());
		String execOutput = output.readFully();
		ExecState state = dockerClient.execInspect(execCreation.id());

		LOG.info("Output: {}", execOutput);

		assertEquals("HelloWorld\n", execOutput);
		assertEquals(Integer.valueOf(0), state.exitCode());
	}

	@Test
	public void testExecution_TryTwoContainersWithSameName_Error() throws InterruptedException,
			IOException, DockerException {
		Exception exc = null;
		try {
			containerFactory.runContainer(CONTAINER_NAME);
			containerFactory.runContainer(CONTAINER_NAME);
		} catch (Exception e) {
			exc = e;
		}
		assertTrue(exc instanceof ContainerRenameConflictException || exc instanceof DockerRequestException);
	}

	@Test
	public void testExecution_CheckContainerRunning_Success() throws InterruptedException,
			IOException, DockerException {

		containerFactory.runContainer(CONTAINER_NAME);
		ContainerInfo inspect = dockerClient.inspectContainer(CONTAINER_NAME);

		assertTrue(inspect.state().running());
	}

	@Test
	public void testStartAndInspection_StartContainerWithEnvs_EnvFound() throws InterruptedException,
			IOException, DockerException {
		Map<String, String> envs = new HashMap<>();
		envs.put("CROW_CRON", "* * * * *");
		containerFactory.runContainer(CONTAINER_NAME, envs);
		ContainerInfo inspect = dockerClient.inspectContainer(CONTAINER_NAME);

		assertTrue(inspect.config().env().contains("CROW_CRON=* * * * *"));
	}

}
