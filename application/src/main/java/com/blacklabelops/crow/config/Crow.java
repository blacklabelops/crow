package com.blacklabelops.crow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


@ConfigurationProperties(prefix="crow")
public class Crow implements IConfigModel {

    @Valid
    @UniqueJobNames
    private List<JobConfiguration> jobs = new ArrayList<>();

    public Crow() {
        super();
    }

    public List<JobConfiguration> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobConfiguration> jobs) {
        this.jobs = jobs;
    }
}
