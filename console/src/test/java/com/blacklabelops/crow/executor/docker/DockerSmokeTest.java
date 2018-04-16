package com.blacklabelops.crow.executor.docker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class DockerSmokeTest {
	
	private Logger LOG = LoggerFactory.getLogger(DockerSmokeTest.class);
	
	public static DockerTestContainerFactory containerFactory;
	
	public static int numberOfContainers = 5;
	
	public static List<String> containers = new ArrayList<>(numberOfContainers);
	
	public static DockerClient dockerClient;
	
	@BeforeClass
	public static void setupClass() throws DockerCertificateException, DockerException, InterruptedException  {
		dockerClient = DockerClientFactory.initializeDockerClient();
		containerFactory = new DockerTestContainerFactory(dockerClient);
		containerFactory.runSomeContainers(numberOfContainers);
	}
	
	@AfterClass
	public static void tearDownClass() {
		containerFactory.deleteContainers();
	}

	@Test
	public void testInfo_AccessingInfo_InfoAccessible() throws DockerException, InterruptedException {
		Info info = dockerClient.info();
		
		assertNotNull(info);
	}
	
	@Test
	public void testListContainers_ContainersStarted_ContainersCanBeFound() throws DockerException, InterruptedException {
		List<Container> containers = dockerClient.listContainers();
		List<String> startedContainers = containerFactory.getContainerIds();
		
		assertTrue(assertContainersFound(startedContainers,containers));
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
		List<String> startedContainers = containerFactory.getContainerIds();
		
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
		List<String> startedContainers = containerFactory.getContainerIds();
		
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
	
	/**
	 * Does not work on circleci (api version 1.32 available, 1.35 required)
	 */
	@Test
	@Ignore
	public void testExecution_ConfigWithWorkingDir_CommandExecutedInWorkingDir() throws InterruptedException, IOException, DockerException {
		String[] command = new String[] { "pwd" };
        String container = containerFactory.getContainerIds().stream().findFirst().get();
        ExecCreation execCreation = dockerClient.execCreate(container, command, DockerClient.ExecCreateParam.attachStdout(),
        		    DockerClient.ExecCreateParam.attachStderr(), new ExecCreateParam("WorkingDir", "/var/www"));
        
        LogStream output = dockerClient.execStart(execCreation.id());
        String execOutput = output.readFully();
        ExecState state = dockerClient.execInspect(execCreation.id());
        
		LOG.info("Output: {}", execOutput);
		
		assertEquals("/var/www\n", execOutput);
		assertEquals(Integer.valueOf(0), state.exitCode());
	}
}
