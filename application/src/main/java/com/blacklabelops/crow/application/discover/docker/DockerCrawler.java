package com.blacklabelops.crow.application.discover.docker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.application.discover.GlobalExtractor;
import com.blacklabelops.crow.application.discover.JobConverter;
import com.blacklabelops.crow.application.discover.JobExtractor;
import com.blacklabelops.crow.application.discover.enironment.LocalDiscoverConfiguration;
import com.blacklabelops.crow.application.model.CrowConfiguration;
import com.blacklabelops.crow.application.model.GlobalCrowConfiguration;
import com.blacklabelops.crow.application.repository.JobRepository;
import com.blacklabelops.crow.docker.client.DiscoveredContainer;
import com.blacklabelops.crow.docker.client.IDockerClient;
import com.blacklabelops.crow.docker.client.spotify.DockerClientFactory;
import com.cronutils.utils.StringUtils;
import com.spotify.docker.client.messages.ContainerInfo;

@Component
public class DockerCrawler {

	public static Logger LOG = LoggerFactory.getLogger(DockerCrawler.class);

	private final LocalDiscoverConfiguration prefixConfiguration;

	private RepositoryUpdater repositoryUpdater;

	@Autowired
	public DockerCrawler(Optional<JobRepository> repository, LocalDiscoverConfiguration prefixConfiguration) {
		super();
		repository.ifPresent(r -> {
			this.repositoryUpdater = new RepositoryUpdater(r);
		});
		this.prefixConfiguration = prefixConfiguration;
	}

	public List<CrowConfiguration> discoverJobs() {
		List<CrowConfiguration> jobs = new ArrayList<>();
		jobs.addAll(discoverDockerJobs());
		return jobs;
	}

	private List<CrowConfiguration> discoverDockerJobs() {
		List<CrowConfiguration> jobs = new ArrayList<>();
		IDockerClient dockerClient = DockerClientFactory.initializeDockerClient();
		try {
			List<DiscoveredContainer> discoveredJobs = dockerClient.discoverDockerJobs();
			jobs.addAll(discoveredJobs.stream().map(job -> inspectionToJobConfiguration(job)).flatMap(l -> l.stream()).collect(Collectors.toList()));
			LOG.debug("Inspecting environment, jobs found: {}", jobs.size());
			if (this.repositoryUpdater != null) {
				repositoryUpdater.notifyRepository(jobs);
			}
		} catch (Exception e) {
			LOG.error("Error crawling Docker jobs!", e);
		}
		return jobs;
	}

	private List<CrowConfiguration> inspectionToJobConfiguration(DiscoveredContainer inf) {
		List<CrowConfiguration> jobs = null;
		Optional<Map<String, String>> envs = inf.getEnvs();
		Optional<Map<String, String>> props = inf.getProps();		
		Optional<GlobalCrowConfiguration> global = extractGlobalConfiguration(envs, props);
		jobs = extractJobs(envs, props);
		if (global.isPresent()) {
			JobConverter converter = new JobConverter(global.get());
			jobs = jobs.stream().map(j -> converter.convertJob(j)).collect(Collectors.toList());
		}
		jobs = jobs.stream().map(j -> setContainer(j, inf)).collect(Collectors.toList());
		return jobs;
	}

	private CrowConfiguration setContainer(CrowConfiguration j, DiscoveredContainer inf) {
		CrowConfiguration cf = j;				
		cf = cf.withContainerName(inf.getContainerName());
		cf = cf.withContainerId(inf.getContainerId());
		return cf;
	}

	private List<CrowConfiguration> extractJobs(Optional<Map<String, String>> envs,
			Optional<Map<String, String>> props) {
		List<CrowConfiguration> jobs = new ArrayList<>();
		if (envs.isPresent()) {
			JobExtractor extractor = new JobExtractor(prefixConfiguration.getStandardJobEnvPrefix());
			jobs.addAll(extractor.extractFromEnvironmentVariables(envs.get()));
		}
		if (props.isPresent()) {
			JobExtractor extractor = new JobExtractor(prefixConfiguration.getStandardJobPrefix());
			jobs.addAll(extractor.extractFromProperties(props.get()));
		}
		return jobs;
	}

	private Optional<GlobalCrowConfiguration> extractGlobalConfiguration(Optional<Map<String, String>> envs, Optional<Map<String, String>> props) {
		Optional<GlobalCrowConfiguration> foundGlobal = Optional.empty();
		if (envs.isPresent()) {
			GlobalExtractor extractorEnv = new GlobalExtractor(prefixConfiguration.getStandardGlobalEnvPrefix());
			foundGlobal = extractorEnv.extractGlobalFromEnvironmentVariables(envs.get());
		}
		if (!foundGlobal.isPresent() && props.isPresent()) {
			GlobalExtractor extractorProps = new GlobalExtractor(prefixConfiguration.getStandardGlobalPrefix());
			foundGlobal = extractorProps.extractGlobalFromProperties(props.get());
		}
		return foundGlobal;
	}	

}
