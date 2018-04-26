package com.blacklabelops.crow.application.discover.docker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import com.blacklabelops.crow.console.executor.docker.DockerClientFactory;
import com.cronutils.utils.StringUtils;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
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
		DockerClient dockerClient = DockerClientFactory.initializeDockerClient();
		try {
			List<Container> containers = dockerClient.listContainers();
			List<List<CrowConfiguration>> inspections = containers.stream().map(c -> {
				try {
					ContainerInfo inf = dockerClient.inspectContainer(c.id());
					List<CrowConfiguration> foundJobs = inspectionToJobConfiguration(inf);
					return foundJobs;
				} catch (DockerException | InterruptedException e) {
					LOG.error("Error inspecting container {}!", c.id());
					return null;
				}
			}).filter(Objects::nonNull).collect(Collectors.toList());
			jobs.addAll(inspections.stream().flatMap(l -> l.stream()).collect(Collectors.toList()));
			LOG.debug("Inspecting environment, jobs found: {}", jobs.size());
			if (this.repositoryUpdater != null) {
				repositoryUpdater.notifyRepository(jobs);
			}
		} catch (DockerException | InterruptedException e) {
			LOG.error("Error crawling Docker jobs!", e);
		}
		return jobs;
	}

	private List<CrowConfiguration> inspectionToJobConfiguration(ContainerInfo inf) {
		List<CrowConfiguration> jobs = null;
		Optional<Map<String, String>> envs = Optional.empty();
		if (inf.config() != null && inf.config().env() != null && !inf.config().env().isEmpty()) {
			envs = Optional.of(envsToMap(inf.config().env()));
		}
		Optional<Map<String, String>> props = Optional.empty();
		if (inf.config() != null && inf.config().labels() != null && !inf.config().labels().isEmpty()) {
			props = Optional.of(inf.config().labels());
		}
		Optional<GlobalCrowConfiguration> global = extractGlobalConfiguration(inf, envs, props);
		jobs = extractJobs(inf, envs, props);
		if (global.isPresent()) {
			JobConverter converter = new JobConverter(global.get());
			jobs = jobs.stream().map(j -> converter.convertJob(j)).collect(Collectors.toList());
		}
		jobs = jobs.stream().map(j -> setContainer(j, inf)).collect(Collectors.toList());
		return jobs;
	}

	private CrowConfiguration setContainer(CrowConfiguration j, ContainerInfo inf) {
		CrowConfiguration cf = j;
		if (!StringUtils.isEmpty(inf.name())) {
			cf = cf.withContainerName(inf.name());
		}
		if (!StringUtils.isEmpty(inf.id())) {
			cf = cf.withContainerId(inf.id());
		}
		return cf;
	}

	private List<CrowConfiguration> extractJobs(ContainerInfo inf, Optional<Map<String, String>> envs,
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

	private Optional<GlobalCrowConfiguration> extractGlobalConfiguration(ContainerInfo inf,
			Optional<Map<String, String>> envs, Optional<Map<String, String>> props) {
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

	private Map<String, String> envsToMap(List<String> envs) {
		Map<String, String> map = new HashMap<>();
		if (envs != null && !envs.isEmpty()) {
			for (String env : envs) {
				fromStringToMap(env, map);
			}
		}
		return map;
	}

	private void fromStringToMap(String env, Map<String, String> map) {
		String[] parts = env.split("=", 2);
		if (parts != null && parts.length == 2) {
			map.put(parts[0], parts[1]);
		}
	}

}
