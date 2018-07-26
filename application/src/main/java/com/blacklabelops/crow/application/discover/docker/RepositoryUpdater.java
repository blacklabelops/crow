package com.blacklabelops.crow.application.discover.docker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.application.model.CrowConfiguration;
import com.blacklabelops.crow.application.repository.JobRepository;
import com.blacklabelops.crow.console.definition.JobId;

class RepositoryUpdater {

	public static Logger LOG = LoggerFactory.getLogger(RepositoryUpdater.class);

	private final JobRepository repository;

	private Map<DockerConfigKey, CrowConfiguration> lastDiscoveredJobs = new HashMap<>();

	private Map<DockerConfigKey, JobId> addedJobs = new HashMap<>();

	public RepositoryUpdater(JobRepository repository) {
		super();
		this.repository = repository;
	}

	public synchronized void notifyRepository(List<CrowConfiguration> jobs) {
		List<CrowConfiguration> newJobs = resolveNewJobs(jobs);
		List<CrowConfiguration> jobUpdates = resolveJobUpdates(jobs);
		List<CrowConfiguration> deletedJobs = resolveJobDeletions(jobs);
		notifyNewJobs(newJobs);
		notifyDeletedJobs(deletedJobs);
		notifyJobUpdates(jobUpdates);
		lastDiscoveredJobs.clear();
		jobs.stream().forEach(j -> {
			DockerConfigKey key = DockerConfigKey.create(j);
			lastDiscoveredJobs.put(key, j);
		});
	}

	private void notifyJobUpdates(List<CrowConfiguration> jobUpdates) {
		jobUpdates.stream().forEach(ju -> {
			DockerConfigKey key = DockerConfigKey.create(ju);
			try {
				this.repository.updateJob(this.addedJobs.get(key), ju);
			} catch (ConstraintViolationException e) {
				LOG.debug("Job invalid and ignored: {}", ju);
				e.getConstraintViolations().stream().forEach(cv -> LOG.debug("Constraint violation: {}", cv
						.getMessage()));
				this.repository.removeJob(this.addedJobs.get(key));
				this.addedJobs.remove(key);
			}
		});

	}

	private void notifyDeletedJobs(List<CrowConfiguration> deletedJobs) {
		deletedJobs.stream().forEach(dj -> {
			DockerConfigKey key = DockerConfigKey.create(dj);
			JobId id = this.addedJobs.get(key);
			if (id != null) {
				this.repository.removeJob(id);
				this.addedJobs.remove(key);
			}
		});

	}

	private void notifyNewJobs(List<CrowConfiguration> newJobs) {
		newJobs.stream().forEach(j -> {
			DockerConfigKey key = DockerConfigKey.create(j);
			try {
				JobId id = this.repository.addJob(j);
				this.addedJobs.put(key, id);
			} catch (ConstraintViolationException e) {
				LOG.debug("Job invalid and ignored: {}", j);
				e.getConstraintViolations().stream().forEach(cv -> LOG.debug("Constraint violation: {}", cv
						.getMessage()));
			}
		});
	}

	private List<CrowConfiguration> resolveJobDeletions(List<CrowConfiguration> jobs) {
		List<CrowConfiguration> deletedJobs = new ArrayList<>(lastDiscoveredJobs.size());
		Map<DockerConfigKey, CrowConfiguration> discoveredJobs = new HashMap<>(jobs.size());
		jobs.stream().forEach(j -> discoveredJobs.put(DockerConfigKey.create(j), j));
		for (DockerConfigKey key : lastDiscoveredJobs.keySet()) {
			if (!discoveredJobs.containsKey(key)) {
				deletedJobs.add(lastDiscoveredJobs.get(key));
			}
		}
		return deletedJobs;
	}

	private List<CrowConfiguration> resolveJobUpdates(List<CrowConfiguration> jobs) {
		return jobs
				.stream()
				.filter(j -> lastDiscoveredJobs.containsKey(DockerConfigKey.create(j))
						&& !j.equals(lastDiscoveredJobs.get(DockerConfigKey.create(j)))
						&& this.addedJobs.containsKey(DockerConfigKey.create(j)))
				.collect(Collectors.toList());
	}

	private List<CrowConfiguration> resolveNewJobs(List<CrowConfiguration> jobs) {
		return jobs
				.stream()
				.filter(j -> !lastDiscoveredJobs.containsKey(DockerConfigKey.create(j))
						&& !this.addedJobs.containsKey(DockerConfigKey.create(j)))
				.collect(Collectors.toList());
	}
}
