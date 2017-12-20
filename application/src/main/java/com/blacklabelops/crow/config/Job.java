package com.blacklabelops.crow.config;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.util.*;

/**
 * Created by steffenbleul on 28.12.16.
 */
public class Job implements IConfigModel {

    @NotEmpty(message = "A unique name for each job has to be defined!")
    private String name;

    @Cron(message = "Cron expression must be valid!")
    private String cron;

    @NotEmpty(message = "Your command is not allowed to be empty!")
    private String command;

    private String execution;

    private String errorMode;

    @Valid
    private Map<String, String> environments = new HashMap<>();

    public Job() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, String> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Map<String, String> environments) {
        this.environments = environments;
    }

    public String getExecution() {
        return execution;
    }

    public void setExecution(String execution) {
        this.execution = execution;
    }

    public String getErrorMode() {
        return errorMode;
    }

    public void setErrorMode(String errorMode) {
        this.errorMode = errorMode;
    }
}
