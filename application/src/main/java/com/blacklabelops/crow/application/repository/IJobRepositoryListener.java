package com.blacklabelops.crow.application.repository;

import com.blacklabelops.crow.console.definition.Job;

public interface IJobRepositoryListener {

	void jobAdded(Job addedJobDefinition);

	void jobRemoved(Job removedJobDefinition);

}
