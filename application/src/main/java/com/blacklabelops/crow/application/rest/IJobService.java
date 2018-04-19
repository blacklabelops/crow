package com.blacklabelops.crow.application.rest;

import java.util.List;

import com.blacklabelops.crow.console.definition.JobId;

public interface IJobService {

	List<JobInformation> listJobs();

	JobDescription getJobDescription(JobId jobName);

}
