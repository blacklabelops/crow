package com.blacklabelops.crow.application.discover.file;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.application.config.Crow;
import com.blacklabelops.crow.application.config.Global;
import com.blacklabelops.crow.application.config.JobConfiguration;
import com.blacklabelops.crow.application.discover.JobConfigurationConverter;
import com.blacklabelops.crow.application.discover.JobConverter;
import com.blacklabelops.crow.application.util.CrowConfiguration;
import com.blacklabelops.crow.application.util.GlobalCrowConfiguration;

@Component
public class ConfigFileDiscover {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigFileDiscover.class);

	private Crow crowConfig;

	@Autowired
	public ConfigFileDiscover(Crow crowConfig) {
		super();
		this.crowConfig = crowConfig;
	}

	public List<CrowConfiguration> discoverJobs() {
		return this.crowConfig
				.getJobs()
				.stream().map(j -> createConfigurationJob(j, this.crowConfig.getGlobal()))
				.collect(Collectors.toList());
	}

	private CrowConfiguration createConfigurationJob(JobConfiguration j, Global global) {
		JobConfigurationConverter crowConverter = new JobConfigurationConverter();
		GlobalCrowConfiguration crowGlobal = null;
		if (this.crowConfig.getGlobal() != null) {
			crowGlobal = crowConverter.convertGlobal(this.crowConfig.getGlobal());
		} else {
			crowGlobal = GlobalCrowConfiguration.builder().build();
		}
		CrowConfiguration crowConfig = crowConverter.convertJob(j);
		JobConverter converter = new JobConverter(crowGlobal);
		CrowConfiguration config = converter.convertJob(crowConfig);
		LOG.debug("Found configuration for job: {}", config);
		return config;
	}

}
