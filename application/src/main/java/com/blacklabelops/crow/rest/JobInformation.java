package com.blacklabelops.crow.rest;

import java.util.Date;

public class JobInformation {
	
	private String name;

    private String cron;
    
    private String nextExecution;
    
    private String execution;

    private String errorMode;
    
    public JobInformation() {
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

	public String getNextExecution() {
		return nextExecution;
	}

	public void setNextExecution(String nextExecution) {
		this.nextExecution = nextExecution;
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
