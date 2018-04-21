package com.blacklabelops.crow.application.discover.enironment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalDiscoverConfiguration {

	@Value("${crow.global.property.prefix:crow.}")
	private String standardGlobalPrefix;

	@Value("${CROW_GLOBAL_PROPERTY_PREFIX:CROW_}")
	private String standardGlobalEnvPrefix;

	@Value("${crow.job.property.prefix:job.}")
	private String standardJobPrefix;

	@Value("${CROW_JOB_PROPERTY_PREFIX:JOB}")
	private String standardJobEnvPrefix;

	public LocalDiscoverConfiguration() {
		super();
	}

	public String getStandardGlobalPrefix() {
		return standardGlobalPrefix;
	}

	public void setStandardGlobalPrefix(String standardGlobalPrefix) {
		this.standardGlobalPrefix = standardGlobalPrefix;
	}

	public String getStandardGlobalEnvPrefix() {
		return standardGlobalEnvPrefix;
	}

	public void setStandardGlobalEnvPrefix(String standardGlobalEnvPrefix) {
		this.standardGlobalEnvPrefix = standardGlobalEnvPrefix;
	}

	public String getStandardJobPrefix() {
		return standardJobPrefix;
	}

	public void setStandardJobPrefix(String standardJobPrefix) {
		this.standardJobPrefix = standardJobPrefix;
	}

	public String getStandardJobEnvPrefix() {
		return standardJobEnvPrefix;
	}

	public void setStandardJobEnvPrefix(String standardJobEnvPrefix) {
		this.standardJobEnvPrefix = standardJobEnvPrefix;
	}

}
