package com.blacklabelops.crow.rest;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.config.JobConfiguration;
import com.blacklabelops.crow.definition.ErrorMode;
import com.blacklabelops.crow.definition.ExecutionMode;
import com.blacklabelops.crow.repository.JobRepository;
import com.blacklabelops.crow.scheduler.CronUtilsExecutionTime;
import com.cronutils.utils.StringUtils;

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
		List<JobConfiguration> jobs = repository.listJobs();
		List<JobInformation> descriptions = new ArrayList<>(jobs.size());
		for (JobConfiguration job : jobs) {
			JobInformation description = new JobInformation();
			BeanUtils.copyProperties(job, description);
			if (!StringUtils.isEmpty(job.getCron())) {
				CronUtilsExecutionTime time = new CronUtilsExecutionTime(job.getCron());
				ZonedDateTime nextExecution = time.nextExecution(ZonedDateTime.now());
				Date nextDateExecution = Date.from(nextExecution.toInstant());
				description.setNextExecution(nextDateExecution);
				if (job.getErrorMode() != null) {
					description.setErrorMode(job.getErrorMode().toString().toLowerCase());
				} else {
					description.setErrorMode(ErrorMode.CONTINUE.toString().toLowerCase());
				}
				if (job.getExecution() != null ) {
					description.setExecution(job.getExecution().toString().toLowerCase());
				} else {
					description.setExecution(ExecutionMode.SEQUENTIAL.toString().toLowerCase());
				}
			}
			descriptions.add(description);
		}
		return descriptions;
	}

	@Override
	public JobDescription getJobDescription(String jobName) {
		JobDescription foundJob = new JobDescription();
		Optional<JobConfiguration> job = repository.findJob(jobName);
		job.ifPresent(j -> BeanUtils.copyProperties(j, foundJob));
		return foundJob;
	}
	
}
