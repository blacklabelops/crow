package com.blacklabelops.crow.repository;

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

import com.blacklabelops.crow.config.Global;
import com.blacklabelops.crow.config.JobConfiguration;
import com.blacklabelops.crow.definition.JobDefinition;

@Component
public class JobRepository {

	private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();;

	private static Logger LOG = LoggerFactory.getLogger(JobRepository.class);

	private Global globalConfiguration;

	private final Map<String, RepositoryJob> jobs = Collections.synchronizedMap(new HashMap<>());

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

	public void addJob(JobConfiguration jobConfiguration) {
		JobConverter jobConverter = new JobConverter(globalConfiguration);
		RepositoryJob repositoryJob = jobConverter.convertJob(jobConfiguration);
		if (validateJob(repositoryJob.getEvaluatedConfiguration())) {
			RepositoryJob addedJob = jobs.putIfAbsent(repositoryJob.getJobDefinition().resolveJobId(), repositoryJob);
			if (addedJob == null) {
				LOG.debug("Job added to repository: {}", jobConfiguration);
				notifyJobAdded(repositoryJob.getJobDefinition());
			} else {
				LOG.info("Job not added, already existent in repository: {}", jobConfiguration);
			}
		}
	}

	private boolean validateJob(JobConfiguration jobConfiguration) {
		Set<ConstraintViolation<JobConfiguration>> errors = validator.validate(jobConfiguration);
		if (!errors.isEmpty()) {
			LOG.error("Job definition invalid, jobname: {}", jobConfiguration.getName());
			errors.stream().forEach(error -> LOG.error(error.getMessage()));
		}
		return errors.isEmpty();
	}

	public boolean jobExists(String jobName) {
		return this.jobs.containsKey(jobName);
	}

	public void removeJob(String jobName) {
		RepositoryJob repositoryJob = this.jobs.remove(jobName);
		if (repositoryJob != null) {
			JobDefinition removedJob = repositoryJob.getJobDefinition();
			LOG.debug("Job {} removed from repository.", jobName);
			notifyJobRemoved(removedJob);
		} else {
			LOG.debug("Job {} not removed, job not found in repository.", jobName);
		}
	}

	public List<JobConfiguration> listJobs() {
		return Arrays
				.asList(this.jobs.values().toArray(new RepositoryJob[this.jobs.size()]))
				.stream()
				.map(job -> new JobConfiguration(job.getJobConfiguration()))
				.collect(Collectors.toList());
	}

	public Optional<JobConfiguration> findJob(String jobName) {
		LOG.debug("Repository listing job: {}", jobName);
		RepositoryJob found = this.jobs.get(jobName);
		if (found != null) {
			JobConfiguration clone = new JobConfiguration(found.getJobConfiguration());
			return Optional.of(clone);
		} else {
			return Optional.empty();
		}
	}

	private void notifyJobRemoved(JobDefinition removedJob) {
		JobDefinition jobClone = new JobDefinition(removedJob);
		List<WeakReference<IJobRepositoryListener>> notifications = new ArrayList<>(listeners);
		notifications
				.parallelStream()
				.filter(notified -> notified.get() != null)
				.forEach(notification -> notification.get().jobRemoved(jobClone));
	}

	private void notifyJobAdded(JobDefinition jobDefinition) {
		JobDefinition jobClone = new JobDefinition(jobDefinition);
		List<WeakReference<IJobRepositoryListener>> notifications = new ArrayList<>(listeners);
		notifications
				.parallelStream()
				.filter(notified -> notified.get() != null)
				.forEach(notification -> notification.get().jobAdded(jobClone));
	}

	public Global getGlobalConfiguration() {
		return new Global(globalConfiguration);
	}

	public void setGlobalConfiguration(Global globalConfiguration) {
		this.globalConfiguration = new Global(globalConfiguration);
	}

}
