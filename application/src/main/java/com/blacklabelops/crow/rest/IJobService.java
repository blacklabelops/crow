package com.blacklabelops.crow.rest;

import java.util.List;

public interface IJobService {

	List<JobInformation> listJobs();

	JobDescription getJobDescription(String jobName);

}
