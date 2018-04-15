package com.blacklabelops.crow.executor.docker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;

public class DockerSmokeTest {
	
	private Logger LOG = LoggerFactory.getLogger(DockerSmokeTest.class);
	
	public static DockerTestContainerFactory containerFactory;
	
	public static int numberOfContainers = 5;
	
	public static List<String> containers = new ArrayList<>(numberOfContainers);
	
	public static DockerClient dockerClient;
	
	@BeforeClass
	public static void setupClass() throws InterruptedException, IOException {
		dockerClient = DockerClientBuilder.getInstance().build();
		containerFactory = new DockerTestContainerFactory(dockerClient);
		containerFactory.runSomeContainers(numberOfContainers);
	}
	
	@AfterClass
	public static void tearDownClass() {
		containerFactory.deleteContainers();
	}

	@Test
	public void testInfo_AccessingInfo_InfoAccessible() {
		Info info = dockerClient.infoCmd().exec();
		
		assertNotNull(info);
	}
	
	@Test
	public void testListContainers_ContainersStarted_ContainersCanBeFound() {
		List<Container> containers = dockerClient.listContainersCmd().exec();
		List<String> startedContainers = containerFactory.getContainerIds();
		
		assertTrue(assertContainersFound(startedContainers,containers));
	}
	
	private boolean assertContainersFound(List<String> startedContainers, List<Container> containers) {
		boolean found = true;
		for (String startedContainer : startedContainers) {
			if (!containers.stream().filter(c -> startedContainer.equals(c.getId())).findFirst().isPresent()) {
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
		List<InspectContainerResponse> inspections = startedContainers.stream().map(c -> dockerClient.inspectContainerCmd(c).exec()).collect(Collectors.toList());
		long endTime = System.currentTimeMillis();
		LOG.info("Execution time {}", endTime - startTime);
		
		assertEquals(numberOfContainers, inspections.size());
	}
	
	@Test
	public void testInspectContainers_ParallelInspection() {
		List<String> startedContainers = containerFactory.getContainerIds();
		
		long startTime = System.currentTimeMillis();
		List<InspectContainerResponse> inspections = startedContainers.parallelStream().map(c -> dockerClient.inspectContainerCmd(c).exec()).collect(Collectors.toList());
		long endTime = System.currentTimeMillis();
		LOG.info("Execution time {}", endTime - startTime);
		
		assertEquals(numberOfContainers, inspections.size());
	}
	
	@Test
	public void testExecution_ExecuteInRunningContainer() throws InterruptedException, IOException {
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
		String container = containerFactory.getContainerIds().stream().findFirst().get();
		ExecCreateCmdResponse response = dockerClient.execCreateCmd(container)
			.withAttachStdout(true)
			.withAttachStderr(true)
			.withCmd("echo","HelloWorld")
			.exec();
		dockerClient.execStartCmd(response.getId()).exec(
                new ExecStartResultCallback(stdout, stderr)).awaitCompletion().close();;
		
		String output = new String(stdout.toByteArray());
		LOG.info("Output: {}", output);
		
		assertEquals("HelloWorld\n", output);
	}
}
