package com.blacklabelops.crow.application.repository;

import com.blacklabelops.crow.console.definition.JobDefinition;

public interface IJobRepositoryListener {

	void jobAdded(JobDefinition addedJobDefinition);

	void jobRemoved(JobDefinition removedJobDefinition);

}
