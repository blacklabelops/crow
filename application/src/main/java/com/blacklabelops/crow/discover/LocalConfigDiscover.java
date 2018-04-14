package com.blacklabelops.crow.discover;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.config.Global;
import com.blacklabelops.crow.config.JobConfiguration;

@Component
public class LocalConfigDiscover {
	
	private final LocalDiscoverConfiguration configuration;

	public LocalConfigDiscover(@Autowired LocalDiscoverConfiguration configuration) {
		super();
		this.configuration = configuration;
	}
	
	public Optional<Global> discoverGlobalConfiguration() {
		Optional<Global> global = new GlobalExtractor(configuration.getStandardGlobalPrefix()).extractGlobalFromProperties(getProperties());
		if (!global.isPresent()) {
			global = new GlobalExtractor(this.configuration.getStandardGlobalEnvPrefix()).extractGlobalFromEnvironmentVariables(System.getenv());
		}
		return global;
	}
	
	public List<JobConfiguration> discoverJobs() {
		List<JobConfiguration> jobs = new ArrayList<>();
		jobs.addAll(new JobExtractor(configuration.getStandardJobPrefix()).extractFromProperties(getProperties()));
		jobs.addAll(new JobExtractor(configuration.getStandardJobEnvPrefix()).extractFromEnvironmentVariables(System.getenv()));
		return jobs;
	}

	private Map<String, String> getProperties() {
		Properties props = System.getProperties();
		Map<String, String> properties = new HashMap<>(props.size());
		props.stringPropertyNames().stream().forEach(name -> properties.put(name, props.getProperty(name)));
		return properties;
	}
}
