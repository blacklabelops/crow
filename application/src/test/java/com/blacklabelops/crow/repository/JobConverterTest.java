package com.blacklabelops.crow.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.blacklabelops.crow.config.Global;
import com.blacklabelops.crow.config.JobConfiguration;
import com.blacklabelops.crow.executor.ErrorMode;
import com.blacklabelops.crow.executor.ExecutionMode;

public class JobConverterTest {
	
	public JobConverter converter;
	
	public Global global;
	
	public JobConfiguration config;
	
	@Before
	public void setup() {
		global = new Global();
		converter = new JobConverter(global);
		config = new JobConfiguration();
	}
	
	@Test
	public void testConvertJob_ShellCommandGlobal_TakeGlobal() {
		global.setShellCommand("shell");
		
		RepositoryJob job = converter.convertJob(config);
		
		assertEquals("shell", job.getJobDefinition().getShellCommand());
	}
	
	@Test
	public void testConvertJob_ShellCommand_TakeNotGlobal() {
		global.setShellCommand("shell");
		config.setShellCommand("myShell");
		
		RepositoryJob job = converter.convertJob(config);
		
		assertEquals("myShell", job.getJobDefinition().getShellCommand());
	}
	
	@Test
	public void testConvertJob_WorkingDirectoryGlobal_TakeGlobal() {
		global.setWorkingDirectory("directory");
		
		RepositoryJob job = converter.convertJob(config);
		
		assertEquals("directory", job.getJobDefinition().getWorkingDir());
	}
	
	@Test
	public void testConvertJob_WorkingDirectorySet_TakeNotGlobal() {
		global.setWorkingDirectory("directory");
		config.setWorkingDirectory("myDirectory");
		
		RepositoryJob job = converter.convertJob(config);
		
		assertEquals("myDirectory", job.getJobDefinition().getWorkingDir());
	}
	
	@Test
	public void testConvertJob_ExecutionModeSet_TakeGlobal() {
		global.setExecution("parallel");
		
		RepositoryJob job = converter.convertJob(config);
		
		assertEquals(ExecutionMode.PARALLEL, job.getJobDefinition().getExecutionMode());
	}
	
	@Test
	public void testConvertJob_ExecutionModeNotSet_DefaultExecutionMode() {
		RepositoryJob job = converter.convertJob(config);
		
		assertEquals(ExecutionMode.SEQUENTIAL, job.getJobDefinition().getExecutionMode());
	}
	
	@Test
	public void testConvertJob_ExecutionModeGlobalSet_TakeGlobal() {
		global.setExecution("parallel");
		
		RepositoryJob job = converter.convertJob(config);
		
		assertEquals(ExecutionMode.PARALLEL, job.getJobDefinition().getExecutionMode());
	}
	
	@Test
	public void testConvertJob_ExecutionModeSet_TakeNotGlobal() {
		global.setExecution("sequential");
		config.setExecution("parallel");
		
		RepositoryJob job = converter.convertJob(config);
		
		assertEquals(ExecutionMode.PARALLEL, job.getJobDefinition().getExecutionMode());
	}
	
	@Test
	public void testConvertJob_ErrorModeNotSet_DefaultErrorMode() {
		RepositoryJob job = converter.convertJob(config);
		
		assertEquals(ErrorMode.CONTINUE, job.getJobDefinition().getErrorMode());
	}
	
	@Test
	public void testConvertJob_ErrorModeGlobalSet_TakeGlobal() {
		global.setErrorMode("stop");
		
		RepositoryJob job = converter.convertJob(config);
		
		assertEquals(ErrorMode.STOP, job.getJobDefinition().getErrorMode());
	}
	
	@Test
	public void testConvertJob_ErrorModeSet_TakeNotGlobal() {
		global.setErrorMode("continue");
		config.setErrorMode("stop");
		
		RepositoryJob job = converter.convertJob(config);
		
		assertEquals(ErrorMode.STOP, job.getJobDefinition().getErrorMode());
	}
	
	@Test
	public void testConvertJob_EnvironmentVariableGloballySet_AdddGlobalEnvironmentVariable() {
		Map<String, String> variables = new HashMap<>();
		variables.put("variable", "value");
		global.setEnvironments(variables);
		
		RepositoryJob job = converter.convertJob(config);
		
		assertEquals(1, job.getJobDefinition().getEnvironmentVariables().size());
		assertTrue(job.getJobDefinition().getEnvironmentVariables().containsKey("variable"));
	}
	
	@Test
	public void testConvertJob_EnvironmentVariablesGloballyAndLocallySet_AdddGlobalEnvironmentVariable() {
		Map<String, String> variables = new HashMap<>();
		variables.put("variable", "value");
		global.setEnvironments(variables);
		Map<String, String> configVariables = new HashMap<>();
		variables.put("variable2", "value");
		config.setEnvironments(configVariables);
		
		RepositoryJob job = converter.convertJob(config);
		
		assertEquals(2, job.getJobDefinition().getEnvironmentVariables().size());
		assertTrue(job.getJobDefinition().getEnvironmentVariables().containsKey("variable"));
		assertTrue(job.getJobDefinition().getEnvironmentVariables().containsKey("variable2"));
	}
	
	@Test
	public void testConvertJob_SameEnvironmentVariableGloballyAndLocallySet_LocalOverwritesGlobal() {
		Map<String, String> variables = new HashMap<>();
		variables.put("variable", "value");
		global.setEnvironments(variables);
		Map<String, String> configVariables = new HashMap<>();
		variables.put("variable", "value2");
		config.setEnvironments(configVariables);
		
		RepositoryJob job = converter.convertJob(config);
		
		assertEquals(1, job.getJobDefinition().getEnvironmentVariables().size());
		assertEquals("value2", job.getJobDefinition().getEnvironmentVariables().get("variable"));
	}
	
	
}
