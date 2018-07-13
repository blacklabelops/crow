package com.blacklabelops.crow.application.discover.docker;

import com.blacklabelops.crow.application.model.CrowConfiguration;
import com.blacklabelops.crow.console.definition.JobId;

class DockerConfigKey {

	private String jobName;

	private String container;

	private JobId repositoryId;

	public DockerConfigKey() {
		super();
	}

	public static DockerConfigKey create(CrowConfiguration jobConfiguration) {
		DockerConfigKey key = new DockerConfigKey();
		key.setJobName(jobConfiguration.getJobName().orElse(null));
		key.setContainer(resolveContainer(jobConfiguration));
		return key;
	}

	private static String resolveContainer(CrowConfiguration jobConfiguration) {
		String container = null;
		if (jobConfiguration.getContainerName().isPresent()) {
			container = jobConfiguration.getContainerName().get();
		} else if (jobConfiguration.getContainerId().isPresent()) {
			container = jobConfiguration.getContainerId().get();
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

	@Override
	public String toString() {
		return String.format("DockerConfigKey [jobName=%s, container=%s, repositoryId=%s]", jobName, container,
				repositoryId);
	}

}
