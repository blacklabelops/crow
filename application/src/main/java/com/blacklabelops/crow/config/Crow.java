package com.blacklabelops.crow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


@ConfigurationProperties(prefix="crow")
public class Crow implements IConfigModel {

    @Valid
    @UniqueJobNames
    private List<Job> jobs = new ArrayList<>();

    public Crow() {
        super();
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
}
