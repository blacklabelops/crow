package com.blacklabelops.crow.application.rest;

import java.util.List;

public interface IJobService {

	List<JobInformation> listJobs();

	JobDescription getJobDescription(String jobName);

}
