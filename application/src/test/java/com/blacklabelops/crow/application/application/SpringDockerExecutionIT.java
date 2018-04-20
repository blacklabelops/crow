package com.blacklabelops.crow.application.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.blacklabelops.crow.application.CrowDemon;
import com.blacklabelops.crow.application.dispatcher.JobDispatcher;
import com.blacklabelops.crow.application.repository.JobRepository;
import com.blacklabelops.crow.application.util.CrowConfiguration;
import com.blacklabelops.crow.console.definition.JobId;
import com.blacklabelops.crow.console.executor.docker.DockerClientFactory;
import com.blacklabelops.crow.console.executor.docker.DockerTestContainerFactory;
import com.blacklabelops.crow.console.logger.IJobLogger;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CrowDemon.class)
@TestPropertySource(locations = "classpath:testDockerExecution.properties")
public class SpringDockerExecutionIT implements IJobLogger {

	public static final String CONTAINER_NAME = "SpringDockerExecutionIT";

	private Logger LOG = LoggerFactory.getLogger(SpringDockerExecutionIT.class);

	public DockerTestContainerFactory containerFactory;

	public List<String> containers = new LinkedList<>();

	public DockerClient dockerClient;

	public ByteArrayOutputStream outStream;

	public ByteArrayOutputStream outErrorStream;

	public List<IJobLogger> logger;

	public CountDownLatch latch;

	@Autowired
	JobRepository repository;

	@Autowired
	JobDispatcher dispatcher;

	@Before
	public void setup() throws DockerCertificateException, DockerException, InterruptedException {
		dockerClient = DockerClientFactory.initializeDockerClient();
		containerFactory = new DockerTestContainerFactory(dockerClient);
		logger = new ArrayList<>();
		logger.add(this);
		latch = new CountDownLatch(1);
	}

	@After
	public void tearDown() {
		containerFactory.deleteContainers();
	}

	@Test
	public void testRepository_ListJobs_TwoDockerJobsAdded() {
		List<CrowConfiguration> jobs = repository.listJobs();

		assertEquals(Integer.valueOf(1), Integer.valueOf(jobs.size()));
		jobs.stream().forEach(j -> assertNotNull(j.getContainerName().orElse(null)));
	}

	@Test
	public void testExecution_ExecuteManually_OutputCorrect() throws DockerException, InterruptedException {
		Optional<CrowConfiguration> job = repository.listJobs().stream().filter(j -> "EchoDockerContainer".equals(j
				.getJobName().orElse(null))).findFirst();
		this.containerFactory.runContainer(CONTAINER_NAME);

		this.dispatcher.execute(JobId.of(job.get().getJobId().orElse(null)), null, logger);
		latch.await();

		String output = new String(this.outStream.toByteArray());
		assertEquals("Hello Docker World!\n", output);
	}

	@Test
	public void testExecution_ExecuteTestExecutionManually_OutputCorrect() throws DockerException,
			InterruptedException {
		Optional<CrowConfiguration> job = repository.listJobs().stream().filter(j -> "EchoDockerContainer".equals(j
				.getJobName().orElse(null))).findFirst();
		this.containerFactory.runContainer(CONTAINER_NAME);

		this.dispatcher.testExecute(JobId.of(job.get().getJobId().orElse(null)), null, logger);
		latch.await();

		String output = new String(this.outStream.toByteArray());
		assertEquals("Hello Docker World!\n", output);
	}

	@Override
	public void initializeLogger() {
		this.outStream = new ByteArrayOutputStream();
		this.outErrorStream = new ByteArrayOutputStream();
	}

	@Override
	public void finishLogger() {
		try {
			outStream.close();
			outErrorStream.close();
		} catch (IOException e) {
			LOG.error("Error closing streams!", e);
			throw new RuntimeException(e);
		}
		latch.countDown();
	}

	@Override
	public OutputStream getInfoLogConsumer() {
		return this.outStream;
	}

	@Override
	public OutputStream getErrorLogConsumer() {
		return this.outErrorStream;
	}

}
