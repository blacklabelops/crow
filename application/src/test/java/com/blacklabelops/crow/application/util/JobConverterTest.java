package com.blacklabelops.crow.application.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.blacklabelops.crow.application.discover.JobConverter;
import com.blacklabelops.crow.console.definition.ErrorMode;
import com.blacklabelops.crow.console.definition.ExecutionMode;

public class JobConverterTest {

	public CrowConfiguration config;

	public GlobalCrowConfiguration global;

	@Before
	public void setup() {
		global = GlobalCrowConfiguration.builder().build();
		config = CrowConfiguration.builder()
				.jobName("Name")
				.cron("* * * * *").build();
	}

	@Test
	public void testConvertJob_ShellCommandGlobal_TakeGlobal() {
		global = global.withShellCommand("shell");
		JobConverter converter = new JobConverter(global);

		CrowConfiguration job = converter.convertJob(config);

		assertEquals("shell", job.getShellCommand().orElse(null));
	}

	@Test
	public void testConvertJob_ShellCommand_TakeNotGlobal() {
		global = global.withShellCommand("shell");
		config = config.withShellCommand("myShell");
		JobConverter converter = new JobConverter(global);

		CrowConfiguration job = converter.convertJob(config);

		assertEquals("myShell", job.getShellCommand().orElse(null));
	}

	@Test
	public void testConvertJob_WorkingDirectoryGlobal_TakeGlobal() {
		global = global.withWorkingDirectory("directory");
		JobConverter converter = new JobConverter(global);

		CrowConfiguration job = converter.convertJob(config);

		assertEquals("directory", job.getWorkingDirectory().orElse(null));
	}

	@Test
	public void testConvertJob_WorkingDirectorySet_TakeNotGlobal() {
		global = global.withWorkingDirectory("directory");
		config = config.withWorkingDirectory("myDirectory");
		JobConverter converter = new JobConverter(global);

		CrowConfiguration job = converter.convertJob(config);

		assertEquals("myDirectory", job.getWorkingDirectory().orElse(null));
	}

	@Test
	public void testConvertJob_ExecutionModeSet_TakeGlobal() {
		global = global.withExecution("parallel");
		JobConverter converter = new JobConverter(global);

		CrowConfiguration job = converter.convertJob(config);

		assertEquals(ExecutionMode.PARALLEL.toString().toLowerCase(), job.getExecution().orElse(null));
	}

	@Test
	public void testConvertJob_ExecutionModeNotSet_DefaultExecutionMode() {
		JobConverter converter = new JobConverter(global);

		CrowConfiguration job = converter.convertJob(config);

		assertEquals(ExecutionMode.SEQUENTIAL.toString().toLowerCase(), job.getExecution().orElse(null));
	}

	@Test
	public void testConvertJob_ExecutionModeGlobalSet_TakeGlobal() {
		global = global.withExecution("parallel");
		JobConverter converter = new JobConverter(global);

		CrowConfiguration job = converter.convertJob(config);

		assertEquals(ExecutionMode.PARALLEL.toString().toLowerCase(), job.getExecution().orElse(null));
	}

	@Test
	public void testConvertJob_ExecutionModeSet_TakeNotGlobal() {
		global = global.withExecution("sequential");
		config = config.withExecution("parallel");
		JobConverter converter = new JobConverter(global);

		CrowConfiguration job = converter.convertJob(config);

		assertEquals(ExecutionMode.PARALLEL.toString().toLowerCase(), job.getExecution().orElse(null));
	}

	@Test
	public void testConvertJob_ErrorModeNotSet_DefaultErrorMode() {
		JobConverter converter = new JobConverter(global);

		CrowConfiguration job = converter.convertJob(config);

		assertEquals(ErrorMode.CONTINUE.toString().toLowerCase(), job.getErrorMode().orElse(null));
	}

	@Test
	public void testConvertJob_ErrorModeGlobalSet_TakeGlobal() {
		global = global.withErrorMode("stop");
		JobConverter converter = new JobConverter(global);

		CrowConfiguration job = converter.convertJob(config);

		assertEquals(ErrorMode.STOP.toString().toLowerCase(), job.getErrorMode().orElse(null));
	}

	@Test
	public void testConvertJob_ErrorModeSet_TakeNotGlobal() {
		global = global.withErrorMode("continue");
		config = config.withErrorMode("stop");
		JobConverter converter = new JobConverter(global);

		CrowConfiguration job = converter.convertJob(config);

		assertEquals(ErrorMode.STOP.toString().toLowerCase(), job.getErrorMode().orElse(null));
	}

	@Test
	public void testConvertJob_EnvironmentVariableGloballySet_AdddGlobalEnvironmentVariable() {
		Map<String, String> variables = new HashMap<>();
		variables.put("variable", "value");
		global = global.withEnvironments(variables);
		JobConverter converter = new JobConverter(global);

		CrowConfiguration job = converter.convertJob(config);

		assertEquals(1, job.getEnvironments().orElse(null).size());
		assertTrue(job.getEnvironments().get().containsKey("variable"));
	}

	@Test
	public void testConvertJob_EnvironmentVariablesGloballyAndLocallySet_AdddGlobalEnvironmentVariable() {
		Map<String, String> variables = new HashMap<>();
		variables.put("variable", "value");
		global = global.withEnvironments(variables);
		Map<String, String> configVariables = new HashMap<>();
		variables.put("variable2", "value");
		config = config.withEnvironments(configVariables);
		JobConverter converter = new JobConverter(global);

		CrowConfiguration job = converter.convertJob(config);

		assertEquals(2, job.getEnvironments().get().size());
		assertTrue(job.getEnvironments().get().containsKey("variable"));
		assertTrue(job.getEnvironments().get().containsKey("variable2"));
	}

	@Test
	public void testConvertJob_SameEnvironmentVariableGloballyAndLocallySet_LocalOverwritesGlobal() {
		Map<String, String> variables = new HashMap<>();
		variables.put("variable", "value");
		global = global.withEnvironments(variables);
		Map<String, String> configVariables = new HashMap<>();
		variables.put("variable", "value2");
		config = config.withEnvironments(configVariables);
		JobConverter converter = new JobConverter(global);

		CrowConfiguration job = converter.convertJob(config);

		assertEquals(1, job.getEnvironments().get().size());
		assertEquals("value2", job.getEnvironments().get().get("variable"));
	}

}
