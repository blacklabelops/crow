package com.blacklabelops.crow.executor.docker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.ExecState;

public class DockerSingleContainerSmokeTest {

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
		String containerName = "TestContainer";
		String[] command = new String[] { "echo", "HelloWorld" };
		containerFactory.runContainer(containerName);
		ExecCreation execCreation = dockerClient.execCreate(containerName, command, DockerClient.ExecCreateParam
				.attachStdout(),
				DockerClient.ExecCreateParam.attachStderr());
		LogStream output = dockerClient.execStart(execCreation.id());
		String execOutput = output.readFully();
		ExecState state = dockerClient.execInspect(execCreation.id());

		LOG.info("Output: {}", execOutput);

		assertEquals("HelloWorld\n", execOutput);
		assertEquals(Integer.valueOf(0), state.exitCode());
	}

	@Test(expected = ContainerRenameConflictException.class)
	public void testExecution_TryTwoContainersWithSameName_Error() throws InterruptedException,
			IOException, DockerException {
		String containerName = "TestContainer";
		containerFactory.runContainer(containerName);
		containerFactory.runContainer(containerName);
	}

	@Test
	public void testExecution_CheckContainerRunning_Success() throws InterruptedException,
			IOException, DockerException {
		String containerName = "TestContainer";

		containerFactory.runContainer(containerName);
		ContainerInfo inspect = dockerClient.inspectContainer(containerName);

		assertTrue(inspect.state().running());
	}

}
