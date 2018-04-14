package com.blacklabelops.crow.repository;

import com.blacklabelops.crow.config.JobConfiguration;
import com.blacklabelops.crow.definition.JobDefinition;

class RepositoryJob {
	
	private JobConfiguration jobConfiguration;
	
	private JobConfiguration evaluatedConfiguration;
	
	private JobDefinition jobDefinition;
	
	public RepositoryJob() {
		super();
	}

	public JobConfiguration getJobConfiguration() {
		return jobConfiguration;
	}

	public void setJobConfiguration(JobConfiguration jobConfiguration) {
		this.jobConfiguration = jobConfiguration;
	}

	public JobDefinition getJobDefinition() {
		return jobDefinition;
	}

	public void setJobDefinition(JobDefinition jobDefinition) {
		this.jobDefinition = jobDefinition;
	}

	public JobConfiguration getEvaluatedConfiguration() {
		return evaluatedConfiguration;
	}

	public void setEvaluatedConfiguration(JobConfiguration evaluatedConfiguration) {
		this.evaluatedConfiguration = evaluatedConfiguration;
	}
	
}
