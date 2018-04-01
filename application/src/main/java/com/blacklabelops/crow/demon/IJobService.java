package com.blacklabelops.crow.demon;

import java.util.List;

import com.blacklabelops.crow.rest.JobDescription;
import com.blacklabelops.crow.rest.JobInformation;

public interface IJobService {

	List<JobInformation> listJobs();

	JobDescription getJobDescription(String jobName);

}
