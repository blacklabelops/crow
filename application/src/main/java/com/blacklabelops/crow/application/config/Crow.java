package com.blacklabelops.crow.application.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "crow")
public class Crow implements IConfigModel {

	private List<JobConfiguration> jobs = new ArrayList<>();

	private Global global;

	public Crow() {
		super();
	}

	public List<JobConfiguration> getJobs() {
		return jobs;
	}

	public void setJobs(List<JobConfiguration> jobs) {
		this.jobs = jobs;
	}

	public Global getGlobal() {
		return global;
	}

	public void setGlobal(Global global) {
		this.global = global;
	}

}
