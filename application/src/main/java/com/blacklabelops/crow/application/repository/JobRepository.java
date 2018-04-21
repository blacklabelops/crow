package com.blacklabelops.crow.application.repository;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.blacklabelops.crow.application.model.CrowConfiguration;
import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.definition.JobId;

@Component
@Validated
public class JobRepository {

	private static Logger LOG = LoggerFactory.getLogger(JobRepository.class);

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

	public JobId addJob(@Valid CrowConfiguration jobConfiguration) {
		JobId result = null;
		RepositoryJobConverter jobConverter = new RepositoryJobConverter();
		RepositoryJob repositoryJob = jobConverter.convertJob(jobConfiguration);
		if (nonExistentJob(repositoryJob.getJobConfiguration())) {
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

	private boolean nonExistentJob(CrowConfiguration evaluatedConfiguration) {
		boolean jobFound = true;
		List<RepositoryJob> jobList = Arrays.asList(this.jobs.values().toArray(new RepositoryJob[this.jobs.size()]));
		for (RepositoryJob job : jobList) {
			Job definition = job.getJobDefinition();
			if (definition.getName().equals(evaluatedConfiguration.getJobName().get())) {
				// Job with Docker Container Name => Job Name with Container Name must be unique
				if (definition.getContainerName().isPresent() && definition.getContainerName().get().equals(
						evaluatedConfiguration.getContainerName().get())) {
					// Job with Docker Container Id => Job Name with Container Id must be unique
				} else if (definition.getContainerId().isPresent() && definition.getContainerId().get().equals(
						evaluatedConfiguration.getContainerId().get())) {
					jobFound = false;
					// Job without Docker Container Name => Job Name must be unique
				} else if (!definition.getContainerName().isPresent() && !definition.getContainerId().isPresent()) {
					jobFound = false;
				}
			}
		}
		return jobFound;
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

	public List<CrowConfiguration> listJobs() {
		return Arrays
				.asList(this.jobs.values().toArray(new RepositoryJob[this.jobs.size()]))
				.stream()
				.map(job -> job.getJobConfiguration())
				.collect(Collectors.toList());
	}

	public Optional<CrowConfiguration> findJob(JobId jobId) {
		LOG.debug("Repository listing job: {}", jobId);
		RepositoryJob found = this.jobs.get(jobId);
		if (found != null) {
			return Optional.of(found.getJobConfiguration());
		} else {
			return Optional.empty();
		}
	}

	private void notifyJobRemoved(Job removedJob) {
		List<WeakReference<IJobRepositoryListener>> notifications = new ArrayList<>(listeners);
		notifications
				.parallelStream()
				.map(notified -> notified.get())
				.filter(Objects::nonNull)
				.forEach(notification -> notification.jobRemoved(removedJob));
	}

	private void notifyJobAdded(Job jobDefinition) {
		List<WeakReference<IJobRepositoryListener>> notifications = new ArrayList<>(listeners);
		notifications
				.parallelStream()
				.map(notified -> notified.get())
				.filter(Objects::nonNull)
				.forEach(notification -> notification.jobAdded(jobDefinition));
	}

}
