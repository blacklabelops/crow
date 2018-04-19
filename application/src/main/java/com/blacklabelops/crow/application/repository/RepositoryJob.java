package com.blacklabelops.crow.application.repository;

import com.blacklabelops.crow.application.config.JobConfiguration;
import com.blacklabelops.crow.console.definition.Job;

class RepositoryJob {

	private JobConfiguration jobConfiguration;

	private JobConfiguration evaluatedConfiguration;

	private Job jobDefinition;

	public RepositoryJob() {
		super();
	}

	public JobConfiguration getJobConfiguration() {
		return jobConfiguration;
	}

	public void setJobConfiguration(JobConfiguration jobConfiguration) {
		this.jobConfiguration = jobConfiguration;
	}

	public Job getJobDefinition() {
		return jobDefinition;
	}

	public void setJobDefinition(Job jobDefinition) {
		this.jobDefinition = jobDefinition;
	}

	public JobConfiguration getEvaluatedConfiguration() {
		return evaluatedConfiguration;
	}

	public void setEvaluatedConfiguration(JobConfiguration evaluatedConfiguration) {
		this.evaluatedConfiguration = evaluatedConfiguration;
	}

}
