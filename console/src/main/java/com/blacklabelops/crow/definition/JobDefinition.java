package com.blacklabelops.crow.definition;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blacklabelops.crow.executor.ErrorMode;
import com.blacklabelops.crow.executor.ExecutionMode;


public class JobDefinition implements IJobDefinition {
	
	private String cron;
	
    private List<String> command;
    
    private List<String> preCommand;
    
    private List<String> postCommand;

    private Map<String, String> environmentVariables;

    private ExecutionMode executorMode = null;

    private ErrorMode errorMode = null;

    private String jobName;

    private File workingDir;
    
    private Integer timeoutMinutes;

    public JobDefinition() {
        super();
    }
    
    @Override
    public String getCron() {
		return cron;
	}
    
    @Override
	public void setCron(String cron) {
		this.cron = cron;
	}

	@Override
    public List<String> getCommand() {
		if (command != null) {
			return new ArrayList<String>(command);
		} else {
			return null;
		}
        
    }

    @Override
    public void setCommand(List<String> command) {
    		if (command != null) {
    			this.command = new ArrayList<String>(command);
    		} else {
    			this.command = null;
    		}
    }

    @Override
    public void setCommand(String... command) {
    		if (command != null) {
    			this.command = new ArrayList<String>(Arrays.asList(command));
    		} else {
    			this.command = null;
    		}
    }
    
    @Override
    public List<String> getPreCommand() {
    		if (this.preCommand != null) {
    			return new ArrayList<String>(preCommand);
    		} else {
    			return null;
    		}
    }

    @Override
    public void setPreCommand(List<String> command) {
    		if (command != null) {
    			this.preCommand = new ArrayList<String>(command);
    		} else {
    			this.preCommand = null;
    		}
    		
    }

    @Override
    public void setPreCommand(String... command) {
    		if (command != null) {
    			this.preCommand = new ArrayList<String>(Arrays.asList(command));
    		} else {
    			this.preCommand = null;
    		}
    }
    
    @Override
    public List<String> getPostCommand() {
    		if (postCommand != null) {
    			return new ArrayList<String>(postCommand);
    		} else {
    			return null;
    		}
    }

    @Override
    public void setPostCommand(List<String> command) {
    		if (postCommand != null) {
    			this.postCommand = new ArrayList<String>(command);
    		} else {
    			this.postCommand = null;
    		}
    }

    @Override
    public void setPostCommand(String... command) {
    		if (postCommand != null) {
    			this.postCommand = new ArrayList<String>(Arrays.asList(command));
    		} else {
    			this.postCommand = null;
    		}
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
    		if (environmentVariables != null) {
    			return new HashMap<>(this.environmentVariables);
    		} else {
    			return null;
    		}
    }

    @Override
    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
    		if (environmentVariables != null) {
    			this.environmentVariables = new HashMap<>(environmentVariables);
    		} else {
    			this.environmentVariables = null;
    		}
    }

    @Override
    public ExecutionMode getExecutionMode() {
        return executorMode;
    }

    @Override
    public void setExecutionMode(ExecutionMode executorMode) {
        this.executorMode = executorMode;
    }

    @Override
    public String getJobName() {
        return jobName;
    }

    @Override
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public ErrorMode getErrorMode() {
        return errorMode;
    }

    @Override
    public void setErrorMode(ErrorMode errorMode) {
        this.errorMode = errorMode;
    }

    @Override
    public File getWorkingDir() {
    		if (workingDir!= null) {
    			return new File(workingDir.getAbsolutePath());
    		} else {
    			return null;
    		}
    }

    @Override
    public void setWorkingDir(File workingDir) {
    		if (workingDir != null) {
    			this.workingDir = new File(workingDir.getAbsolutePath());
    		} else {
    			this.workingDir = null;
    		}
    }
    
    @Override
	public Integer getTimeoutMinutes() {
		return timeoutMinutes;
	}
    
    @Override
	public void setTimeoutMinutes(Integer minutes) {
		this.timeoutMinutes = minutes;
	}
    
}
