package com.blacklabelops.crow.application.discover;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.application.util.CrowConfiguration;
import com.blacklabelops.crow.application.util.CrowConfiguration.Builder;
import com.cronutils.utils.StringUtils;

public class JobExtractor {

	private static final Logger LOG = LoggerFactory.getLogger(JobExtractor.class);

	private final String prefix;

	public JobExtractor(String prefix) {
		super();
		this.prefix = prefix;
	}

	public List<CrowConfiguration> extractFromEnvironmentVariables(Map<String, String> environmentVariables) {
		List<CrowConfiguration> jobs = new ArrayList<>();
		int iterator = 0;
		Optional<CrowConfiguration> job = null;
		do {
			String jobPrefix = this.prefix.concat(Integer.toString(++iterator));
			job = extractJobFromEnvironmentVariables(jobPrefix, environmentVariables);
			job.ifPresent(j -> jobs.add(j));
		} while (job.isPresent());
		return jobs;
	}

	public List<CrowConfiguration> extractFromProperties(Map<String, String> properties) {
		List<CrowConfiguration> jobs = new ArrayList<>();
		int iterator = 0;
		Optional<CrowConfiguration> job = null;
		do {
			String jobPrefix = this.prefix.concat(Integer.toString(++iterator));
			job = extractJobFromProperties(jobPrefix, properties);
			job.ifPresent(j -> jobs.add(j));
		} while (job.isPresent());
		return jobs;
	}

	private Optional<CrowConfiguration> extractJobFromEnvironmentVariables(String jobPrefix,
			Map<String, String> environmentVariables) {
		CrowConfiguration job = null;
		if (checkJobEnvironments(jobPrefix, environmentVariables)) {
			job = fillJobFromEnvironmentVariables(jobPrefix, environmentVariables);
		}
		return Optional.ofNullable(job);
	}

	public Optional<String> emptyString(String string) {
		String result = null;
		if (!StringUtils.isEmpty(string)) {
			result = string;
		}
		return Optional.ofNullable(result);
	}

	private CrowConfiguration fillJobFromEnvironmentVariables(String jobPrefix,
			Map<String, String> environmentVariables) {
		Builder builder = CrowConfiguration.builder()
				.jobName(emptyString(environmentVariables.get(getEnvironmentField(jobPrefix, JobField.NAME))))
				.cron(emptyString(environmentVariables.get(getEnvironmentField(jobPrefix, JobField.CRON))))
				.command(emptyString(environmentVariables.get(getEnvironmentField(jobPrefix, JobField.COMMAND))))
				.preCommand(emptyString(environmentVariables.get(getEnvironmentField(jobPrefix, JobField.PRE_COMMAND))))
				.postCommand(emptyString(environmentVariables.get(getEnvironmentField(jobPrefix,
						JobField.POST_COMMAND))))
				.workingDirectory(emptyString(environmentVariables.get(getEnvironmentField(jobPrefix,
						JobField.WORKING_DIRECTORY))))
				.shellCommand(emptyString(environmentVariables.get(getEnvironmentField(jobPrefix,
						JobField.SHELL_COMMAND))))
				.execution(emptyString(environmentVariables.get(getEnvironmentField(jobPrefix,
						JobField.EXECUTION_MODE))))
				.errorMode(emptyString(environmentVariables.get(getEnvironmentField(jobPrefix, JobField.ON_ERROR))))
				.containerName(emptyString(environmentVariables.get(getEnvironmentField(jobPrefix,
						JobField.CONTAINER_NAME))))
				.containerId(emptyString(environmentVariables.get(getEnvironmentField(jobPrefix,
						JobField.CONTAINER_ID))));
		String timeout = environmentVariables.get(getEnvironmentField(jobPrefix, JobField.TIMEOUT_MINUTES));
		try {
			Optional<Integer> timeoutMinutes = evaluateTimeout(timeout);
			builder.timeOutMinutes(timeoutMinutes);
		} catch (NumberFormatException e) {
			LOG.error("Error parsing timeout parameter: {}", timeout);
			return null;
		}
		return builder.build();
	}

	private CrowConfiguration fillGlobalFromProperties(String jobPrefix, Map<String, String> properties) {
		Builder builder = CrowConfiguration.builder()
				.jobName(emptyString(properties.get(getPropertyField(jobPrefix, JobField.NAME))))
				.cron(emptyString(properties.get(getPropertyField(jobPrefix, JobField.CRON))))
				.command(emptyString(properties.get(getPropertyField(jobPrefix, JobField.COMMAND))))
				.preCommand(emptyString(properties.get(getPropertyField(jobPrefix, JobField.PRE_COMMAND))))
				.postCommand(emptyString(properties.get(getPropertyField(jobPrefix, JobField.POST_COMMAND))))
				.workingDirectory(emptyString(properties.get(getPropertyField(jobPrefix, JobField.WORKING_DIRECTORY))))
				.shellCommand(emptyString(properties.get(getPropertyField(jobPrefix, JobField.SHELL_COMMAND))))
				.execution(emptyString(properties.get(getPropertyField(jobPrefix, JobField.EXECUTION_MODE))))
				.errorMode(emptyString(properties.get(getPropertyField(jobPrefix, JobField.ON_ERROR))))
				.containerName(emptyString(properties.get(getPropertyField(jobPrefix, JobField.CONTAINER_NAME))))
				.containerId(emptyString(properties.get(getPropertyField(jobPrefix, JobField.CONTAINER_ID))));
		String timeout = properties.get(getPropertyField(jobPrefix, JobField.TIMEOUT_MINUTES));
		try {
			Optional<Integer> timeoutMinutes = evaluateTimeout(timeout);
			builder.timeOutMinutes(timeoutMinutes);
		} catch (NumberFormatException e) {
			LOG.error("Error parsing timeout parameter: {}", timeout);
			return null;
		}
		return builder.build();
	}

	private Optional<Integer> evaluateTimeout(String minuteString) throws NumberFormatException {
		Integer timeoutMinutes = null;
		if (!StringUtils.isEmpty(minuteString)) {
			timeoutMinutes = Integer.valueOf(minuteString);
		}
		return Optional.ofNullable(timeoutMinutes);
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

	public Optional<CrowConfiguration> extractJobFromProperties(String jobPrefix, Map<String, String> properties) {
		CrowConfiguration job = null;
		if (checkJobProperties(jobPrefix, properties)) {
			job = fillGlobalFromProperties(jobPrefix, properties);
		}
		return Optional.ofNullable(job);
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
