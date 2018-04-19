package com.blacklabelops.crow.application.repository;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.application.config.Global;
import com.blacklabelops.crow.application.config.JobConfiguration;
import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.definition.JobId;

@Component
public class JobRepository {

	private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();;

	private static Logger LOG = LoggerFactory.getLogger(JobRepository.class);

	private Global globalConfiguration;

	private final Map<JobId, RepositoryJob> jobs = Collections.synchronizedMap(new HashMap<>());

	private final List<WeakReference<IJobRepositoryListener>> listeners = Collections.synchronizedList(
			new ArrayList<>());

	public JobRepository() {
		super();
	}

	public void addListeners(List<IJobRepositoryListener> repositoryListeners) {
		repositoryListeners.stream().forEach(listener -> listeners.add(new WeakReference<>(listener)));
	}

	public void addListener(IJobRepositoryListener repositoryListener) {
		listeners.add(new WeakReference<>(repositoryListener));
	}

	public JobId addJob(JobConfiguration jobConfiguration) {
		JobId result = null;
		JobConverter jobConverter = new JobConverter(globalConfiguration);
		RepositoryJob repositoryJob = jobConverter.convertJob(jobConfiguration);
		if (validateJob(repositoryJob.getEvaluatedConfiguration()) && nonExistentJob(repositoryJob
				.getEvaluatedConfiguration())) {
			RepositoryJob addedJob = jobs.putIfAbsent(repositoryJob.getJobDefinition().getId(), repositoryJob);
			if (addedJob == null) {
				LOG.debug("Job added to repository: {}", repositoryJob.getJobDefinition());
				result = repositoryJob.getJobDefinition().getId();
				notifyJobAdded(repositoryJob.getJobDefinition());
			} else {
				LOG.info("Job not added, already existent in repository: {}", jobConfiguration);
			}
		}
		return result;
	}

	private boolean nonExistentJob(JobConfiguration evaluatedConfiguration) {
		boolean jobFound = true;
		List<RepositoryJob> jobList = Arrays.asList(this.jobs.values().toArray(new RepositoryJob[this.jobs.size()]));
		for (RepositoryJob job : jobList) {
			Job definition = job.getJobDefinition();
			if (definition.getName().equals(evaluatedConfiguration.getName())) {
				// Job with Docker Container Name => Job Name with Container Name must be unique
				if (definition.getContainerName().isPresent() && definition.getContainerName().get().equals(
						evaluatedConfiguration.getContainerName())) {
					// Job with Docker Container Id => Job Name with Container Id must be unique
				} else if (definition.getContainerId().isPresent() && definition.getContainerId().get().equals(
						evaluatedConfiguration.getContainerId())) {
					jobFound = false;
					// Job without Docker Container Name => Job Name must be unique
				} else if (!definition.getContainerName().isPresent() && !definition.getContainerId().isPresent()) {
					jobFound = false;
				}
			}
		}
		return jobFound;
	}

	private boolean validateJob(JobConfiguration jobConfiguration) {
		Set<ConstraintViolation<JobConfiguration>> errors = validator.validate(jobConfiguration);
		if (!errors.isEmpty()) {
			LOG.error("Job definition invalid, jobname: {}", jobConfiguration.getName());
			errors.stream().forEach(error -> LOG.error(error.getMessage()));
		}
		return errors.isEmpty();
	}

	public boolean jobExists(JobId jobId) {
		return this.jobs.containsKey(jobId);
	}

	public void removeJob(JobId jobId) {
		RepositoryJob repositoryJob = this.jobs.remove(jobId);
		if (repositoryJob != null) {
			Job removedJob = repositoryJob.getJobDefinition();
			LOG.debug("Job {} removed from repository.", jobId);
			notifyJobRemoved(removedJob);
		} else {
			LOG.debug("Job {} not removed, job not found in repository.", jobId);
		}
	}

	public List<JobConfiguration> listJobs() {
		return Arrays
				.asList(this.jobs.values().toArray(new RepositoryJob[this.jobs.size()]))
				.stream()
				.map(job -> new JobConfiguration(job.getEvaluatedConfiguration()))
				.collect(Collectors.toList());
	}

	public Optional<JobConfiguration> findJob(JobId jobId) {
		LOG.debug("Repository listing job: {}", jobId);
		RepositoryJob found = this.jobs.get(jobId);
		if (found != null) {
			JobConfiguration clone = new JobConfiguration(found.getJobConfiguration());
			return Optional.of(clone);
		} else {
			return Optional.empty();
		}
	}

	private void notifyJobRemoved(Job removedJob) {
		List<WeakReference<IJobRepositoryListener>> notifications = new ArrayList<>(listeners);
		notifications
				.parallelStream()
				.filter(notified -> notified.get() != null)
				.forEach(notification -> notification.get().jobRemoved(removedJob));
	}

	private void notifyJobAdded(Job jobDefinition) {
		List<WeakReference<IJobRepositoryListener>> notifications = new ArrayList<>(listeners);
		notifications
				.parallelStream()
				.filter(notified -> notified.get() != null)
				.forEach(notification -> notification.get().jobAdded(jobDefinition));
	}

	public Global getGlobalConfiguration() {
		return new Global(globalConfiguration);
	}

	public void setGlobalConfiguration(Global globalConfiguration) {
		LOG.debug("Setting global configuration: {}", globalConfiguration);
		this.globalConfiguration = new Global(globalConfiguration);
	}

}
