package com.blacklabelops.crow.console.reporter;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.executor.ExecutionResult;
import com.blacklabelops.crow.console.scheduler.IScheduler;

public class ExecutionErrorReporterTest {

	public ExecutionErrorReporter reporter;

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	IScheduler scheduler;

	@Spy
	ExecutionResult result;

	@Before
	public void setup() {
		reporter = new ExecutionErrorReporter(scheduler);
		Job job = Job.builder().id("id").name("name").build();
		result = new ExecutionResult(job);
	}

	@After
	public void tearDown() {
		reporter = null;
	}

	@Test
	public void testFailingJob_WhenFailing_ThenNotifyError() {
		reporter.failingJob(result);
		verify(scheduler, times(1)).notifyExecutionError(eq(result));
	}

	@Test
	public void testFailingJob_WhenFailingWithReturnCode_ThenNotifyErrorWithReturnCode() {
		Integer errorCode = Integer.valueOf(5);
		result.setReturnCode(errorCode);
		reporter.failingJob(result);
		verify(scheduler, times(1)).notifyExecutionError(eq(result));
	}

}