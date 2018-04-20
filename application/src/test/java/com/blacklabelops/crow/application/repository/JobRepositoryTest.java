package com.blacklabelops.crow.application.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.blacklabelops.crow.application.util.CrowConfiguration;
import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.definition.JobId;

public class JobRepositoryTest {

	private static final String COMMAND = "command";

	private static final String CRON = "* * * * *";

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	public IJobRepositoryListener listener;

	@Test
	public void testAddJob_WhenAddingTwoJobs_SecondWillBeDismissed() {
		CrowConfiguration config = CrowConfiguration.builder()
				.jobName("a")
				.command(COMMAND)
				.cron(CRON).build();
		JobRepository rep = new JobRepository();
		rep.addListener(listener);

		rep.addJob(config);
		rep.addJob(config);

		verify(listener, times(1)).jobAdded(any(Job.class));
	}

	@Test
	public void testAddJob_WhenAddingTwoDifferentJobs_BothWillBeAdded() {
		CrowConfiguration config = CrowConfiguration.builder()
				.jobName("a")
				.command(COMMAND)
				.cron(CRON).build();
		JobRepository rep = new JobRepository();
		rep.addListener(listener);

		rep.addJob(config);
		CrowConfiguration configB = CrowConfiguration.builder()
				.jobName("b")
				.command(COMMAND)
				.cron(CRON).build();
		rep.addJob(configB);

		verify(listener, times(2)).jobAdded(any(Job.class));
	}

	@Test
	public void testRemoveJob_WhenRemovingJob_JobWillBeRemoved() {
		CrowConfiguration config = CrowConfiguration.builder()
				.jobName("a")
				.command(COMMAND)
				.cron(CRON).build();
		JobRepository rep = new JobRepository();
		rep.addListener(listener);
		JobId jobId = rep.addJob(config);

		rep.removeJob(jobId);

		verify(listener, times(1)).jobAdded(any(Job.class));
		verify(listener, times(1)).jobRemoved(any(Job.class));
	}

	@Test
	public void testRemoveJob_WhenRemovingUnknownJob_NoJobWillBeRemoved() {
		CrowConfiguration config = CrowConfiguration.builder()
				.jobName("a")
				.command(COMMAND)
				.cron(CRON).build();
		JobRepository rep = new JobRepository();
		rep.addListener(listener);
		rep.addJob(config);

		rep.removeJob(JobId.of("a"));

		verify(listener, times(1)).jobAdded(any(Job.class));
		verify(listener, never()).jobRemoved(nullable(Job.class));
	}

}
