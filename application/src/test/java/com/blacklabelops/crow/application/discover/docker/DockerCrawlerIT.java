package com.blacklabelops.crow.application.discover.docker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blacklabelops.crow.application.discover.enironment.LocalDiscoverConfiguration;
import com.blacklabelops.crow.application.model.CrowConfiguration;
import com.blacklabelops.crow.docker.client.IDockerClient;
import com.blacklabelops.crow.docker.client.spotify.DockerClientFactory;
import com.blacklabelops.crow.docker.client.spotify.test.DockerTestClientFactory;
import com.blacklabelops.crow.docker.client.test.IDockerClientTest;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

public class DockerCrawlerIT {

	public static final String CONTAINER_NAME = "DockerCrawlerITTestContainer";

	public IDockerClientTest containerFactory;

	public IDockerClient dockerClient;

	public DockerCrawler crawler;

	LocalDiscoverConfiguration local;

	@Before
	public void setupClass() throws DockerCertificateException, DockerException, InterruptedException {
		dockerClient = DockerClientFactory.initializeDockerClient();
		containerFactory = DockerTestClientFactory.initializeDockerClient();
		local = new LocalDiscoverConfiguration();
		local.setStandardGlobalEnvPrefix("CROW_");
		local.setStandardGlobalPrefix("crow.");
		local.setStandardJobEnvPrefix("JOB");
		local.setStandardJobPrefix("job.");
		crawler = new DockerCrawler(Optional.empty(), local);
	}

	@After
	public void tearDownClass() {
		containerFactory.deleteContainers();
	}

	@Test
	public void testDiscover_WhenNoContainerStarted_NoJobsFound() throws DockerException, InterruptedException {
		List<CrowConfiguration> jobs = crawler.discoverJobs();

		assertTrue(jobs.isEmpty());
	}

	@Test
	public void testDiscover_WhenContainerStarted_JobFound() throws DockerException, InterruptedException {
		CrowConfiguration config = CrowConfiguration.builder()
				.cron("* * * * *")
				.jobName("Job")
				.command("echo HelloWorld").build();
		String containerId = containerFactory.runContainer(CONTAINER_NAME, jobToEnvs(config, 1));

		List<CrowConfiguration> jobs = crawler.discoverJobs();

		CrowConfiguration foundJob = jobs.stream().findFirst().get();
		assertEquals(containerId, foundJob.getContainerId().get());
		assertEquals(config.getCron().get(), foundJob.getCron().get());
		assertEquals(config.getJobName().get(), foundJob.getJobName().get());
		assertEquals(config.getCommand().get(), foundJob.getCommand().get());
	}

	@Test
	public void testDiscover_WhenContainerStopped_NoJobsFound() throws DockerException, InterruptedException {
		CrowConfiguration config = CrowConfiguration.builder()
				.cron("* * * * *")
				.jobName("Job")
				.command("echo HelloWorld").build();
		String containerId = containerFactory.runContainer(CONTAINER_NAME, jobToEnvs(config, 1));
		containerFactory.stopContainer(containerId);

		List<CrowConfiguration> jobs = crawler.discoverJobs();

		assertTrue(jobs.isEmpty());
	}

	private Map<String, String> jobToEnvs(CrowConfiguration config, int number) {
		Map<String, String> envs = new HashMap<>();
		String prefix = local.getStandardJobEnvPrefix() + number;
		if (config.getCron().isPresent()) {
			envs.put(prefix + "CRON", config.getCron().get());
		}
		if (config.getJobName().isPresent()) {
			envs.put(prefix + "NAME", config.getJobName().get());
		}
		if (config.getCommand().isPresent()) {
			envs.put(prefix + "COMMAND", config.getCommand().get());
		}
		return envs;
	}

}
