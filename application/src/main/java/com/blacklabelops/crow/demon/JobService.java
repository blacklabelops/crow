package com.blacklabelops.crow.demon;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.config.Job;
import com.blacklabelops.crow.executor.ErrorMode;
import com.blacklabelops.crow.executor.ExecutionMode;
import com.blacklabelops.crow.rest.JobDescription;
import com.blacklabelops.crow.rest.JobInformation;
import com.blacklabelops.crow.scheduler.CronUtilsExecutionTime;
import com.cronutils.utils.StringUtils;

@Component
public class JobService implements IJobService {
	
	private final SchedulerDemon demon;
	
	@Autowired
	public JobService(SchedulerDemon demon) {
		super();
		this.demon = demon;
	}

	@Override
	public List<JobInformation> listJobs() {
		List<Job> jobs = demon.listJobs();
		List<JobInformation> descriptions = new ArrayList<>(jobs.size());
		for (Job job : jobs) {
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
		Optional<Job> job = demon.listJobs().stream().filter(found -> found.getName().equals(jobName)).findFirst();
		job.ifPresent(j -> BeanUtils.copyProperties(j, foundJob));
		return foundJob;
	}
	
}
