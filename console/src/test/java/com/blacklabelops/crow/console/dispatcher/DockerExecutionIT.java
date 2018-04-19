package com.blacklabelops.crow.console.dispatcher;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.definition.JobId;
import com.blacklabelops.crow.console.executor.docker.DockerClientFactory;
import com.blacklabelops.crow.console.executor.docker.DockerExecutorTemplate;
import com.blacklabelops.crow.console.executor.docker.DockerTestContainerFactory;
import com.blacklabelops.crow.console.logger.IJobLogger;
import com.blacklabelops.crow.console.logger.IJobLoggerFactory;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

public class DockerExecutionIT implements IJobLogger, IJobLoggerFactory {

	private Logger LOG = LoggerFactory.getLogger(DockerExecutionIT.class);

	public static final String CONTAINER_NAME = "DockerExecutionITContainer";

	public DockerTestContainerFactory containerFactory;

	public List<String> containers = new LinkedList<>();

	public DockerClient dockerClient;

	public ByteArrayOutputStream outStream;

	public ByteArrayOutputStream outErrorStream;

	public List<IJobLogger> logger;

	public List<IJobLoggerFactory> loggerFactory;

	public CountDownLatch latch;

	public IDispatcher dispatcher;

	@Before
	public void setup() throws DockerCertificateException, DockerException, InterruptedException {
		dockerClient = DockerClientFactory.initializeDockerClient();
		containerFactory = new DockerTestContainerFactory(dockerClient);
		logger = new ArrayList<>();
		logger.add(this);
		loggerFactory = new ArrayList<>();
		loggerFactory.add(this);
		latch = new CountDownLatch(1);
		dispatcher = new Dispatcher();
	}

	@After
	public void tearDown() {
		containerFactory.deleteContainers();
	}

	@Test
	public void testExecution_ExecuteManually_OutputCorrect() throws DockerException, InterruptedException {
		Job definition = Job.builder() //
				.id(JobId.builder().id("EchoTest").build())
				.name("EchoTest") //
				.command(Arrays.asList("echo", "Hello Docker World!")) //
				.containerName(CONTAINER_NAME) //
				.build();
		DockerExecutorTemplate jobTemplate = new DockerExecutorTemplate(definition, null, loggerFactory);
		this.containerFactory.runContainer(CONTAINER_NAME);

		this.dispatcher.addJob(jobTemplate);
		this.dispatcher.execute(definition.getId());
		latch.await();

		assertEquals("Hello Docker World!\n", new String(this.outStream.toByteArray()));
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

	@Override
	public IJobLogger createInstance() {
		return this;
	}

}
