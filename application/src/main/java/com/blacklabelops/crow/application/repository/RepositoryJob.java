package com.blacklabelops.crow.application.repository;

import com.blacklabelops.crow.application.util.CrowConfiguration;
import com.blacklabelops.crow.console.definition.Job;

class RepositoryJob {

	private CrowConfiguration jobConfiguration;

	private Job jobDefinition;

	public RepositoryJob() {
		super();
	}

	public CrowConfiguration getJobConfiguration() {
		return jobConfiguration;
	}

	public void setJobConfiguration(CrowConfiguration jobConfiguration) {
		this.jobConfiguration = jobConfiguration;
	}

	public Job getJobDefinition() {
		return jobDefinition;
	}

	public void setJobDefinition(Job jobDefinition) {
		this.jobDefinition = jobDefinition;
	}

}
