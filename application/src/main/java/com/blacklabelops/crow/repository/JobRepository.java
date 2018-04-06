package com.blacklabelops.crow.repository;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.config.JobConfiguration;
import com.blacklabelops.crow.definition.JobDefinition;

@Component
public class JobRepository {
	
	public static Logger LOG = LoggerFactory.getLogger(JobRepository.class);
	
	private final Map<String,RepositoryJob> jobs = Collections.synchronizedMap(new HashMap<>());
	
	private final List<WeakReference<IJobRepositoryListener>> listeners = Collections.synchronizedList(new ArrayList<>());
	
	public JobRepository() {
		super();
	}
	
	public void addListeners(List<IJobRepositoryListener> repositoryListeners) {
		repositoryListeners.stream().forEach(listener -> 
			listeners.add(new WeakReference<>(listener))
		);
	}
	
	public void addListener(IJobRepositoryListener repositoryListener) {
		listeners.add(new WeakReference<>(repositoryListener));
	}
	
	public void addJob(JobConfiguration jobConfiguration) {
		JobConfiguration clone = cloneConfiguration(jobConfiguration);
		RepositoryJob repJob = new RepositoryJob();
		repJob.setJobConfiguration(clone);
		JobConverter jobConverter = new JobConverter();
		JobDefinition jobDefinition = jobConverter.convertJob(clone);
		repJob.setJobDefinition(jobDefinition);
		RepositoryJob addedJob = jobs.putIfAbsent(jobDefinition.getJobName(), repJob);
		if (addedJob == null) {
			LOG.debug("Job added to repository: {}", clone);
			notifyJobAdded(repJob.getJobDefinition());
		} else {
			LOG.debug("Job not added, already existent in repository: {}", clone);
		}
	}
	
	private JobConfiguration cloneConfiguration(JobConfiguration jobConfiguration) {
		JobConfiguration clonedConfiguration = new JobConfiguration();
		BeanUtils.copyProperties(jobConfiguration, clonedConfiguration);
		return clonedConfiguration;
	}
	
	public boolean jobExists(String jobName) {
		return this.jobs.containsKey(jobName);
	}
	
	public void removeJob(String jobName) {
		RepositoryJob repositoryJob = this.jobs.remove(jobName);
		if (repositoryJob != null) {
			JobDefinition removedJob = repositoryJob.getJobDefinition();
			notifyJobRemoved(removedJob);
		} else {
			LOG.debug("Job {} not removed, job not found in repository.", jobName);
		}
	}
	
	public List<JobConfiguration> listJobs() {
		List<JobConfiguration> jobConfigurations = new ArrayList<>(this.jobs.size());
		Arrays.asList(this.jobs.values().toArray(new JobConfiguration[this.jobs.size()])).stream().forEach(job -> {
			JobConfiguration clonedJob = new JobConfiguration();
			BeanUtils.copyProperties(job, clonedJob);
			jobConfigurations.add(clonedJob);
		});
		return jobConfigurations;
	}
	
	public Optional<JobConfiguration> findJob(String jobName) {
		RepositoryJob found = this.jobs.get(jobName);
		if (found != null) {
			JobConfiguration clone = new JobConfiguration();
			BeanUtils.copyProperties(found.getJobConfiguration(), clone);
			return Optional.of(clone);
		} else {
			return Optional.empty();
		}
	}

	private void notifyJobRemoved(JobDefinition removedJob) {
		JobDefinition jobClone = new JobDefinition();
		BeanUtils.copyProperties(removedJob, jobClone);
		List<WeakReference<IJobRepositoryListener>> notifications = new ArrayList<>(listeners);
		notifications
			.parallelStream()
			.filter(notified -> notified.get() != null)
			.forEach(notification -> notification.get().jobRemoved(jobClone));
	}

	private void notifyJobAdded(JobDefinition jobDefinition) {
		JobDefinition jobClone = new JobDefinition();
		BeanUtils.copyProperties(jobDefinition, jobClone);
		List<WeakReference<IJobRepositoryListener>> notifications = new ArrayList<>(listeners);
		notifications
			.parallelStream()
			.filter(notified -> notified.get() != null)
			.forEach(notification -> notification.get().jobAdded(jobClone));
	}
}
