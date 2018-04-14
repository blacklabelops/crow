package com.blacklabelops.crow.discover;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.config.JobConfiguration;
import com.cronutils.utils.StringUtils;

class JobExtractor {
	
	private static final Logger LOG = LoggerFactory.getLogger(JobExtractor.class);
	
	private final String prefix;
	
	public JobExtractor(String prefix) {
		super();
		this.prefix = prefix;
	}
	
	public List<JobConfiguration> extractFromEnvironmentVariables(Map<String, String> environmentVariables) {
		List<JobConfiguration> jobs = new ArrayList<>();
		int iterator = 0;
		Optional<JobConfiguration> job = null;
		do {
			String jobPrefix = this.prefix.concat(Integer.toString(++iterator));
			job = extractJobFromEnvironmentVariables(jobPrefix, environmentVariables);
			job.ifPresent(j -> jobs.add(j));
		} while (job.isPresent());
		return jobs;
	}
	
	public List<JobConfiguration> extractFromProperties(Map<String, String> properties) {
		List<JobConfiguration> jobs = new ArrayList<>();
		int iterator = 0;
		Optional<JobConfiguration> job = null;
		do {
			String jobPrefix = this.prefix.concat(Integer.toString(++iterator));
			job = extractJobFromProperties(jobPrefix, properties);
			job.ifPresent(j -> jobs.add(j));
		} while (job.isPresent());
		return jobs;
	}
	
	private Optional<JobConfiguration> extractJobFromEnvironmentVariables(String jobPrefix, Map<String, String> environmentVariables) {
		Optional<JobConfiguration> job = Optional.empty();
		if (checkJobEnvironments(jobPrefix, environmentVariables)) {
			job = fillJobFromEnvironmentVariables(jobPrefix, environmentVariables);
		}
		return job;
	}
	
	private Optional<JobConfiguration> fillJobFromEnvironmentVariables(String jobPrefix, Map<String, String> environmentVariables) {
		JobConfiguration job = new JobConfiguration();
		job.setName(environmentVariables.get(getEnvironmentField(jobPrefix, JobField.NAME)));
		job.setCron(environmentVariables.get(getEnvironmentField(jobPrefix, JobField.CRON)));
		job.setCommand(environmentVariables.get(getEnvironmentField(jobPrefix, JobField.COMMAND)));
		job.setPreCommand(environmentVariables.get(getEnvironmentField(jobPrefix, JobField.PRE_COMMAND)));
		job.setPostCommand(environmentVariables.get(getEnvironmentField(jobPrefix, JobField.POST_COMMAND)));
		job.setWorkingDirectory(environmentVariables.get(getEnvironmentField(jobPrefix, JobField.WORKING_DIRECTORY)));
		job.setShellCommand(environmentVariables.get(getEnvironmentField(jobPrefix, JobField.SHELL_COMMAND)));
		job.setExecution(environmentVariables.get(getEnvironmentField(jobPrefix, JobField.EXECUTION_MODE)));
		job.setErrorMode(environmentVariables.get(getEnvironmentField(jobPrefix, JobField.ON_ERROR)));
		String minuteString = environmentVariables.get(getEnvironmentField(jobPrefix, JobField.TIMEOUT_MINUTES));
		try {
			job.setTimeOutMinutes(getTimeoutMinutes(minuteString));
		} catch (NumberFormatException e) {
			LOG.error("Error parsing timeout parameter: {}", minuteString);
			return Optional.empty();
		}
		return Optional.of(job);
	}
	
	private Integer getTimeoutMinutes(String minuteString) {
		Integer minutes = null;
		if (!StringUtils.isEmpty(minuteString)) {
			minutes = Integer.parseInt(minuteString);
		}
		return minutes;
	}

	private Optional<JobConfiguration> fillGlobalFromProperties(String jobPrefix, Map<String, String> properties) {
		JobConfiguration job = new JobConfiguration();
		job.setName(properties.get(getPropertyField(jobPrefix, JobField.NAME)));
		job.setCron(properties.get(getPropertyField(jobPrefix, JobField.CRON)));
		job.setCommand(properties.get(getPropertyField(jobPrefix, JobField.COMMAND)));
		job.setPreCommand(properties.get(getPropertyField(jobPrefix, JobField.PRE_COMMAND)));
		job.setPostCommand(properties.get(getPropertyField(jobPrefix, JobField.POST_COMMAND)));
		job.setWorkingDirectory(properties.get(getPropertyField(jobPrefix, JobField.WORKING_DIRECTORY)));
		job.setShellCommand(properties.get(getPropertyField(jobPrefix, JobField.SHELL_COMMAND)));
		job.setExecution(properties.get(getPropertyField(jobPrefix, JobField.EXECUTION_MODE)));
		job.setErrorMode(properties.get(getPropertyField(jobPrefix, JobField.ON_ERROR)));
		String minuteString = properties.get(getPropertyField(jobPrefix, JobField.TIMEOUT_MINUTES));
		try {
			job.setTimeOutMinutes(getTimeoutMinutes(minuteString));
		} catch (NumberFormatException e) {
			LOG.error("Error parsing timeout parameter: {}", minuteString);
			return Optional.empty();
		}
		return Optional.of(job);
	}

	private boolean checkJobEnvironments(String jobPrefix, Map<String, String> environmentVariables) {
		boolean exists = false;
		for (JobField field : JobField.values()) {
			String checkedField = getEnvironmentField(jobPrefix, field);
			if (environmentVariables.containsKey(checkedField)) {
				exists = true;
			}
		}
		return exists;
	}

	private String getEnvironmentField(String jobPrefix, JobField field) {
		return jobPrefix.concat(field.getEnvironmentName());
	}

	public Optional<JobConfiguration> extractJobFromProperties(String jobPrefix, Map<String, String> properties) {
		Optional<JobConfiguration> job = Optional.empty();
		if (checkJobProperties(jobPrefix, properties)) {
			job = fillGlobalFromProperties(jobPrefix, properties);
		}
		return job;
	}

	private boolean checkJobProperties(String jobPrefix, Map<String, String> properties) {
		boolean exists = false;
		for (JobField field : JobField.values()) {
			String checkedField = getPropertyField(jobPrefix, field);
			if (properties.containsKey(checkedField)) {
				exists = true;
			}
		}
		return exists;
	}

	private String getPropertyField(String jobPrefix, JobField field) {
		return jobPrefix.concat(field.getPropertyName());
	}
}
