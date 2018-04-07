package com.blacklabelops.crow.definition;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.executor.ErrorMode;
import com.blacklabelops.crow.executor.ExecutionMode;

public class JobDefinition {
	
	private static Logger LOG = LoggerFactory.getLogger(JobDefinition.class);
	
	private String cron;
	
	private String shellCommand;
	
    private List<String> command;
    
    private List<String> preCommand;
    
    private List<String> postCommand;

    private Map<String, String> environmentVariables;

    private ExecutionMode executorMode = null;

    private ErrorMode errorMode = null;

    private String jobName;

    private String workingDir;
    
    private Integer timeoutMinutes;

    public JobDefinition() {
        super();
    }
    
    public JobDefinition(JobDefinition anotherJobdefinition) {
        super();
        try {
			BeanUtils.copyProperties(this, anotherJobdefinition);
		} catch (IllegalAccessException | InvocationTargetException e) {
			LOG.error("JobDefinition konnte nicht geclont werden!");
		}
    }
    
    public String getCron() {
		return cron;
	}
    
	public void setCron(String cron) {
		this.cron = cron;
	}
    
    public String getShellCommand() {
        return shellCommand;
    }

    public void setShellCommand(String shellCommand) {
        this.shellCommand = shellCommand;
    }
    
    public List<String> getCommand() {
		if (command != null) {
			return new ArrayList<String>(command);
		} else {
			return null;
		}
        
    }

    public void setCommand(List<String> command) {
    		if (command != null) {
    			this.command = new ArrayList<String>(command);
    		} else {
    			this.command = null;
    		}
    }

    public void setCommand(String... command) {
    		if (command != null) {
    			this.command = new ArrayList<String>(Arrays.asList(command));
    		} else {
    			this.command = null;
    		}
    }
    
    public List<String> getPreCommand() {
    		if (this.preCommand != null) {
    			return new ArrayList<String>(preCommand);
    		} else {
    			return null;
    		}
    }

    public void setPreCommand(List<String> command) {
    		if (command != null) {
    			this.preCommand = new ArrayList<String>(command);
    		} else {
    			this.preCommand = null;
    		}
    		
    }

    public void setPreCommand(String... command) {
    		if (command != null) {
    			this.preCommand = new ArrayList<String>(Arrays.asList(command));
    		} else {
    			this.preCommand = null;
    		}
    }
    
    public List<String> getPostCommand() {
    		if (postCommand != null) {
    			return new ArrayList<String>(postCommand);
    		} else {
    			return null;
    		}
    }

    public void setPostCommand(List<String> command) {
    		if (postCommand != null) {
    			this.postCommand = new ArrayList<String>(command);
    		} else {
    			this.postCommand = null;
    		}
    }

    public void setPostCommand(String... command) {
    		if (postCommand != null) {
    			this.postCommand = new ArrayList<String>(Arrays.asList(command));
    		} else {
    			this.postCommand = null;
    		}
    }

    public Map<String, String> getEnvironmentVariables() {
    		if (environmentVariables != null) {
    			return new HashMap<>(this.environmentVariables);
    		} else {
    			return null;
    		}
    }

    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
    		if (environmentVariables != null) {
    			this.environmentVariables = new HashMap<>(environmentVariables);
    		} else {
    			this.environmentVariables = null;
    		}
    }

    public ExecutionMode getExecutionMode() {
        return executorMode;
    }

    public void setExecutionMode(ExecutionMode executorMode) {
        this.executorMode = executorMode;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public ErrorMode getErrorMode() {
        return errorMode;
    }

    public void setErrorMode(ErrorMode errorMode) {
        this.errorMode = errorMode;
    }

    public String getWorkingDir() {
    		return this.workingDir;
    }

    public void setWorkingDir(String workingDir) {
    		this.workingDir = workingDir;
    }
    
	public Integer getTimeoutMinutes() {
		return timeoutMinutes;
	}
    
	public void setTimeoutMinutes(Integer minutes) {
		this.timeoutMinutes = minutes;
	}
    
}
