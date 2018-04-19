package com.blacklabelops.crow.console.reporter;

import java.time.LocalDateTime;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.executor.ExecutionResult;
import com.blacklabelops.crow.console.executor.IExecutor;

public class ConsoleReporterTest {

	public ConsoleReporter reporter = new ConsoleReporter();

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	IExecutor executor;

	@Test
	public void testStartingJob() {
		Job jobDefinition = Job.builder().name("Name").id("Name").build();
		ExecutionResult result = ExecutionResult.of(jobDefinition);
		result = result.withStartingTime(LocalDateTime.now());

		reporter.startingJob(result);
	}

	@Test
	public void finishedJob() {
		Job jobDefinition = Job.builder().name("Name").id("Name").build();
		ExecutionResult result = ExecutionResult.of(jobDefinition);
		result = result.withFinishingTime(LocalDateTime.now()).withReturnCode(Integer.valueOf(5));

		reporter.finishedJob(result);
	}

}