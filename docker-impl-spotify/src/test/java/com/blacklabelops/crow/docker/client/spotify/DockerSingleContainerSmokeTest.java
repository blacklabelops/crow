package com.blacklabelops.crow.docker.client.spotify;

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

import com.blacklabelops.crow.docker.client.spotify.test.DockerTestClientFactory;
import com.blacklabelops.crow.docker.client.spotify.test.SpotifyDockerTestClient;
import com.blacklabelops.crow.docker.client.test.IDockerClientTest;
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

	public DockerClient dockerClient;
	
	public IDockerClientTest dockerTestClient;

	public List<String> containers = new LinkedList<>();

	@Before
	public void setupClass() throws DockerCertificateException, DockerException, InterruptedException {
		dockerTestClient = DockerTestClientFactory.initializeDockerClient();
		dockerClient = ((SpotifyDockerTestClient) dockerTestClient).getDockerClient();
	}

	@After
	public void tearDownClass() {
		dockerTestClient.deleteContainers();
	}

	@Test
	public void testExecution_ExecuteInCommandNamedContainer_OutputExecuted() throws InterruptedException,
			IOException, DockerException {
		String[] command = new String[] { "echo", "HelloWorld" };
		dockerTestClient.runContainer(CONTAINER_NAME);
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
			dockerTestClient.runContainer(CONTAINER_NAME);
			dockerTestClient.runContainer(CONTAINER_NAME);
		} catch (Exception e) {
			exc = e;
		}
		assertTrue(exc.getCause() instanceof ContainerRenameConflictException || exc.getCause() instanceof DockerRequestException);
	}

	@Test
	public void testExecution_CheckContainerRunning_Success() throws InterruptedException,
			IOException, DockerException {

		dockerTestClient.runContainer(CONTAINER_NAME);
		ContainerInfo inspect = dockerClient.inspectContainer(CONTAINER_NAME);

		assertTrue(inspect.state().running());
	}

	@Test
	public void testStartAndInspection_StartContainerWithEnvs_EnvFound() throws InterruptedException,
			IOException, DockerException {
		Map<String, String> envs = new HashMap<>();
		envs.put("CROW_CRON", "* * * * *");
		dockerTestClient.runContainer(CONTAINER_NAME, envs);
		ContainerInfo inspect = dockerClient.inspectContainer(CONTAINER_NAME);

		assertTrue(inspect.config().env().contains("CROW_CRON=* * * * *"));
	}

}
