package com.blacklabelops.crow.console.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.blacklabelops.crow.console.definition.ErrorMode;
import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.definition.JobId;
import com.blacklabelops.crow.console.dispatcher.DispatcherResult;
import com.blacklabelops.crow.console.dispatcher.DispatchingResult;
import com.blacklabelops.crow.console.executor.ExecutionResult;
import com.blacklabelops.crow.console.executor.IExecutor;

public class JobSchedulerTest {

	public JobScheduler scheduler = new JobScheduler();

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	public IExecutor executor;

	@Test
	public void whenFirstEntryTheNextThenReturnFirstJob() {
		ZonedDateTime dateTime1 = ZonedDateTime.now().plusMinutes(1);
		ScheduledJob job1 = createJob(JobId.of("A"), dateTime1);
		ZonedDateTime dateTime2 = ZonedDateTime.now().plusMinutes(5);
		ScheduledJob job2 = createJob(JobId.of("B"), dateTime2);
		JobScheduler scheduler = new JobScheduler();
		scheduler.addJob(job1);
		scheduler.addJob(job2);

		ScheduledJob nextExecutableJob = scheduler.getNextExecutableJob();

		assertEquals(job1, nextExecutableJob);
	}

	@Test
	public void whenSecondEntryTheNextThenReturnSecondJob() {
		ZonedDateTime dateTime1 = ZonedDateTime.now().plusMinutes(5);
		ScheduledJob job1 = createJob(JobId.of("A"), dateTime1);
		ZonedDateTime dateTime2 = ZonedDateTime.now().plusMinutes(1);
		ScheduledJob job2 = createJob(JobId.of("B"), dateTime2);
		JobScheduler scheduler = new JobScheduler();
		scheduler.addJob(job1);
		scheduler.addJob(job2);

		ScheduledJob nextExecutableJob = scheduler.getNextExecutableJob();

		assertEquals(job2, nextExecutableJob);
	}

	private ScheduledJob createJob(JobId jobId, ZonedDateTime nextExecution) {
		IExecutionTime executorTime = mock(IExecutionTime.class);
		when(executorTime.nextExecution(any())).thenReturn(nextExecution);
		return new ScheduledJob(jobId, executorTime);
	}

	@Test
	public void testNotifyFailingJob_WhenJobFaulty_UnscheduleJob() {
		JobId jobId = JobId.of("A");
		ScheduledJob job = createJob(jobId, ZonedDateTime.now().plusMinutes(1));
		scheduler.addJob(job);
		Job jobDefinition = Job.builder().id(jobId).name("A").errorMode(ErrorMode.STOP).build();
		DispatchingResult result = DispatchingResult.of(jobDefinition, DispatcherResult.DROPPED_ALREADY_RUNNING);

		scheduler.notifyDispatchingError(result);

		assertNull(scheduler.getNextExecutableJob());
	}

	@Test
	public void testNotifyFailingJob_WhenJobContinue_StillScheduleJob() {
		JobId jobId = JobId.of("A");
		ScheduledJob job = createJob(jobId, ZonedDateTime.now().plusMinutes(1));
		scheduler.addJob(job);
		Job jobDefinition = Job.builder().id(jobId).name("A").errorMode(ErrorMode.CONTINUE).build();
		DispatchingResult result = DispatchingResult.of(jobDefinition, DispatcherResult.DROPPED_ALREADY_RUNNING);

		scheduler.notifyDispatchingError(result);

		assertEquals(job, scheduler.getNextExecutableJob());
	}

	@Test
	public void testNotifyFailingJob_WhenNoErrorDefinition_StillScheduleJob() {
		JobId jobId = JobId.of("A");
		ScheduledJob job = createJob(jobId, ZonedDateTime.now().plusMinutes(1));
		scheduler.addJob(job);
		Job jobDefinition = Job.builder().id(jobId).name("A").build();
		DispatchingResult result = DispatchingResult.of(jobDefinition, DispatcherResult.DROPPED_ALREADY_RUNNING);

		scheduler.notifyDispatchingError(result);

		assertEquals(job, scheduler.getNextExecutableJob());
	}

	@Test
	public void testNotifyExecutionError_WhenJobFaulty_UnscheduleJob() {
		JobId jobId = JobId.of("A");
		Job jobDefinition = Job.builder().id(jobId).name("A").errorMode(ErrorMode.STOP).build();
		ScheduledJob job = createJob(jobId, ZonedDateTime.now().plusMinutes(1));
		scheduler.addJob(job);
		ExecutionResult result = new ExecutionResult(jobDefinition);

		scheduler.notifyExecutionError(result);

		assertNull(scheduler.getNextExecutableJob());
	}

	@Test
	public void testNotifyExecutionError_WhenJobContinue_StillScheduleJob() {
		JobId jobId = JobId.of("A");
		Job jobDefinition = Job.builder().id(jobId).name("A").errorMode(ErrorMode.CONTINUE).build();
		ScheduledJob job = createJob(jobId, ZonedDateTime.now().plusMinutes(1));
		scheduler.addJob(job);
		ExecutionResult result = new ExecutionResult(jobDefinition);

		scheduler.notifyExecutionError(result);

		assertEquals(job, scheduler.getNextExecutableJob());
	}

	@Test
	public void testNotifyExecutionError_WhenNoErrorDefinition_StillScheduleJob() {
		JobId jobId = JobId.of("A");
		Job jobDefinition = Job.builder().id(jobId).name("A").build();
		ScheduledJob job = createJob(jobId, ZonedDateTime.now().plusMinutes(1));
		scheduler.addJob(job);
		ExecutionResult result = new ExecutionResult(jobDefinition);

		scheduler.notifyExecutionError(result);

		assertEquals(job, scheduler.getNextExecutableJob());
	}

}
