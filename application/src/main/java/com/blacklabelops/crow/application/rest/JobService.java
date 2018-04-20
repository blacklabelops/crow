package com.blacklabelops.crow.application.rest;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.application.repository.JobRepository;
import com.blacklabelops.crow.application.util.CrowConfiguration;
import com.blacklabelops.crow.console.definition.JobId;
import com.blacklabelops.crow.console.scheduler.CronUtilsExecutionTime;

@Component
public class JobService implements IJobService {

	private final JobRepository repository;

	@Autowired
	public JobService(JobRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public List<JobInformation> listJobs() {
		List<CrowConfiguration> jobs = repository.listJobs();
		List<JobInformation> descriptions = new ArrayList<>(jobs.size());
		for (CrowConfiguration job : jobs) {
			JobInformation description = new JobInformation();
			description.setName(getDescriptionLabel(job));
			description.setCron(job.getCron().orElse(""));
			description.setErrorMode(job.getErrorMode().orElse(""));
			description.setExecution(job.getExecution().orElse(""));
			if (job.getCron().isPresent()) {
				CronUtilsExecutionTime time = new CronUtilsExecutionTime(job.getCron().get());
				ZonedDateTime nextExecution = time.nextExecution(ZonedDateTime.now());
				Date nextDateExecution = Date.from(nextExecution.toInstant());
				description.setNextExecution(nextDateExecution);
			}
			descriptions.add(description);
		}
		return descriptions;
	}

	private String getDescriptionLabel(CrowConfiguration job) {
		StringBuilder label = new StringBuilder();
		label.append(job.getJobName().get());
		if (job.getContainerName().isPresent()) {
			label.append(" - ").append(job.getContainerName().get());
		} else if (job.getContainerId().isPresent()) {
			label.append(" - ").append(job.getContainerId().get());
		}
		return label.toString();
	}

	@Override
	public JobDescription getJobDescription(JobId jobName) {
		JobDescription foundJob = new JobDescription();
		Optional<CrowConfiguration> job = repository.findJob(jobName);
		job.ifPresent(j -> BeanUtils.copyProperties(j, foundJob));
		return foundJob;
	}

}
