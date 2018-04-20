package com.blacklabelops.crow.application.dockercrawler;

import com.blacklabelops.crow.application.config.JobConfiguration;
import com.blacklabelops.crow.console.definition.JobId;
import com.cronutils.utils.StringUtils;

class DockerConfigKey {

	private String jobName;

	private String container;

	private JobId repositoryId;

	public DockerConfigKey() {
		super();
	}

	public static DockerConfigKey create(JobConfiguration jobConfiguration) {
		DockerConfigKey key = new DockerConfigKey();
		key.setJobName(jobConfiguration.getName());
		key.setContainer(resolveContainer(jobConfiguration));
		return key;
	}

	private static String resolveContainer(JobConfiguration jobConfiguration) {
		String container = null;
		if (!StringUtils.isEmpty(jobConfiguration.getContainerName())) {
			container = jobConfiguration.getContainerName();
		} else if (!StringUtils.isEmpty(jobConfiguration.getId())) {
			container = jobConfiguration.getContainerId();
		}
		return container;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((container == null) ? 0 : container.hashCode());
		result = prime * result + ((jobName == null) ? 0 : jobName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DockerConfigKey other = (DockerConfigKey) obj;
		if (container == null) {
			if (other.container != null)
				return false;
		} else if (!container.equals(other.container))
			return false;
		if (jobName == null) {
			if (other.jobName != null)
				return false;
		} else if (!jobName.equals(other.jobName))
			return false;
		return true;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public JobId getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(JobId repositoryId) {
		this.repositoryId = repositoryId;
	}

}
