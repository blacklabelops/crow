package com.blacklabelops.crow.application.discover.enironment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.application.discover.GlobalExtractor;
import com.blacklabelops.crow.application.discover.JobConverter;
import com.blacklabelops.crow.application.discover.JobExtractor;
import com.blacklabelops.crow.application.model.CrowConfiguration;
import com.blacklabelops.crow.application.model.GlobalCrowConfiguration;

@Component
public class LocalConfigDiscover {

	private static final Logger LOG = LoggerFactory.getLogger(LocalConfigDiscover.class);

	private final LocalDiscoverConfiguration configuration;

	public LocalConfigDiscover(@Autowired LocalDiscoverConfiguration configuration) {
		super();
		this.configuration = configuration;
	}

	public Optional<GlobalCrowConfiguration> discoverGlobalConfiguration() {
		LOG.debug("Looking for global configuration with property prefix: {}", configuration.getStandardGlobalPrefix());
		Optional<GlobalCrowConfiguration> global = new GlobalExtractor(configuration.getStandardGlobalPrefix())
				.extractGlobalFromProperties(getProperties());
		if (!global.isPresent()) {
			LOG.debug("Looking for global configuration with env prefix: {}", configuration
					.getStandardGlobalEnvPrefix());
			global = new GlobalExtractor(this.configuration.getStandardGlobalEnvPrefix())
					.extractGlobalFromEnvironmentVariables(System.getenv());
		}
		global.ifPresent(g -> LOG.debug("Found global config () ", g));
		return global;
	}

	public List<CrowConfiguration> discoverJobs() {
		List<CrowConfiguration> jobs = new ArrayList<>();
		LOG.debug("Looking for jobs with job prefix: {})", configuration.getStandardJobPrefix());
		jobs.addAll(new JobExtractor(configuration.getStandardJobPrefix()).extractFromProperties(getProperties()));
		LOG.debug("Looking for jobs with job prefix: {})", configuration.getStandardJobEnvPrefix());
		jobs.addAll(new JobExtractor(configuration.getStandardJobEnvPrefix()).extractFromEnvironmentVariables(System
				.getenv()));
		Optional<GlobalCrowConfiguration> global = extractGlobalConfiguration(System.getenv(), propertiesToMap(System
				.getProperties()));
		if (global.isPresent()) {
			JobConverter converter = new JobConverter(global.get());
			jobs = jobs.stream().map(j -> converter.convertJob(j)).collect(Collectors.toList());
		}
		jobs.stream().forEach(j -> LOG.debug("Discovered local job configuration: {}", j));
		return jobs;
	}

	private Map<String, String> propertiesToMap(Properties properties) {
		Map<String, String> result = new HashMap<>();
		properties.entrySet().stream().forEach(entry -> result.put((String) entry.getKey(), (String) entry.getValue()));
		return result;
	}

	private Optional<GlobalCrowConfiguration> extractGlobalConfiguration(
			Map<String, String> envs, Map<String, String> props) {
		Optional<GlobalCrowConfiguration> foundGlobal = Optional.empty();
		GlobalExtractor extractorEnv = new GlobalExtractor(configuration.getStandardGlobalEnvPrefix());
		foundGlobal = extractorEnv.extractGlobalFromEnvironmentVariables(envs);
		GlobalExtractor extractorProps = new GlobalExtractor(configuration.getStandardGlobalPrefix());
		foundGlobal = extractorProps.extractGlobalFromProperties(props);
		return foundGlobal;
	}

	private Map<String, String> getProperties() {
		Properties props = System.getProperties();
		Map<String, String> properties = new HashMap<>(props.size());
		props.stringPropertyNames().stream().forEach(name -> properties.put(name, props.getProperty(name)));
		return properties;
	}
}
