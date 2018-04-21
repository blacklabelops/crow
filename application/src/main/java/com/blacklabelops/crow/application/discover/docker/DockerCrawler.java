package com.blacklabelops.crow.application.discover.docker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.application.config.Global;
import com.blacklabelops.crow.application.config.JobConfiguration;
import com.blacklabelops.crow.application.discover.enironment.LocalDiscoverConfiguration;
import com.blacklabelops.crow.application.repository.JobRepository;
import com.blacklabelops.crow.console.executor.docker.DockerClientFactory;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;

@Component
public class DockerCrawler {

	public static Logger LOG = LoggerFactory.getLogger(DockerCrawler.class);

	private final JobRepository repository;

	private final LocalDiscoverConfiguration prefixConfiguration;

	private List<JobConfiguration> discoveredJobs;

	private RepositoryUpdater repositoryUpdater;

	@Autowired
	public DockerCrawler(JobRepository repository, LocalDiscoverConfiguration prefixConfiguration) {
		super();
		this.repository = repository;
		this.prefixConfiguration = prefixConfiguration;
		this.repositoryUpdater = new RepositoryUpdater(this.repository);
	}

	public List<JobConfiguration> discoverJobs() {
		List<JobConfiguration> jobs = new ArrayList<>();
		jobs.addAll(discoverDockerJobs());
		return jobs;
	}

	private List<JobConfiguration> discoverDockerJobs() {
		List<JobConfiguration> jobs = new ArrayList<>();
		DockerClient dockerClient = DockerClientFactory.initializeDockerClient();
		try {
			List<Container> containers = dockerClient.listContainers();
			List<List<JobConfiguration>> inspections = containers.stream().map(c -> {
				try {
					ContainerInfo inf = dockerClient.inspectContainer(c.id());
					List<JobConfiguration> foundJobs = inspectionToJobConfiguration(inf);
					return foundJobs;
				} catch (DockerException | InterruptedException e) {
					LOG.error("Error inspecting container {}!", c.id());
					return null;
				}
			}).filter(Objects::nonNull).collect(Collectors.toList());
			jobs.addAll(inspections.stream().flatMap(l -> l.stream()).collect(Collectors.toList()));
			repositoryUpdater.notifyRepository(inspections);
		} catch (DockerException | InterruptedException e) {
			LOG.error("Error crawling Docker jobs!", e);
		}
		return jobs;
	}

	private List<JobConfiguration> inspectionToJobConfiguration(ContainerInfo inf) {
		List<JobConfiguration> jobs = null;
		Optional<Global> global = extractGlobalConfiguration(inf);

		return null;
	}

	private Optional<Global> extractGlobalConfiguration(ContainerInfo inf) {
		// TODO Auto-generated method stub
		return null;
	}

}
