package com.blacklabelops.crow.application.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blacklabelops.crow.application.util.VersionAccessor;
import com.blacklabelops.crow.console.definition.JobId;

@RestController
public class JobController {

	private final IJobService jobService;

	@Autowired
	public JobController(IJobService jobService) {
		this.jobService = jobService;
	}

	@RequestMapping("/crow/jobs")
	public List<JobInformation> listJobs() {
		return jobService.listJobs();
	}

	@RequestMapping("/crow/jobs/{jobId}")
	public JobDescription getJobDescription(@PathVariable String jobId) {
		return jobService.getJobDescription(JobId.of(jobId));
	}

	@RequestMapping("/crow/version")
	public Version getServerVersion() {
		Version version = new Version();
		version.setVersion(new VersionAccessor().getVersion());
		return version;
	}
}
