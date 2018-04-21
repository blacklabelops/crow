package com.blacklabelops.crow.application.discover.docker;

import java.util.List;
import java.util.Map;

import com.blacklabelops.crow.application.config.JobConfiguration;
import com.blacklabelops.crow.application.repository.JobRepository;

class RepositoryUpdater {

	private final JobRepository repository;

	private Map<DockerConfigKey, JobConfiguration> lastDiscoveredJobs;

	public RepositoryUpdater(JobRepository repository) {
		super();
		this.repository = repository;
	}

	public void notifyRepository(List<List<JobConfiguration>> inspections) {

	}
}
