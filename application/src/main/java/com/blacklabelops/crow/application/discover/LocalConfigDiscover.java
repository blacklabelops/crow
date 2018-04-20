package com.blacklabelops.crow.application.discover;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.application.util.CrowConfiguration;
import com.blacklabelops.crow.application.util.GlobalCrowConfiguration;
import com.blacklabelops.crow.application.util.GlobalExtractor;
import com.blacklabelops.crow.application.util.JobExtractor;

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
		jobs.stream().forEach(j -> LOG.debug("Discovered local job configuration: {}", j));
		return jobs;
	}

	private Map<String, String> getProperties() {
		Properties props = System.getProperties();
		Map<String, String> properties = new HashMap<>(props.size());
		props.stringPropertyNames().stream().forEach(name -> properties.put(name, props.getProperty(name)));
		return properties;
	}
}
