package com.blacklabelops.crow.repository;

import com.blacklabelops.crow.definition.JobDefinition;

public interface IJobRepositoryListener {

	void jobAdded(JobDefinition addedJobDefinition);

	void jobRemoved(JobDefinition removedJobDefinition);

}
