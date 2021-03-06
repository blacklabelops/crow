package com.blacklabelops.crow.console.scheduler;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.definition.JobId;
import com.blacklabelops.crow.console.dispatcher.Dispatcher;
import com.blacklabelops.crow.console.dispatcher.IDispatcher;
import com.blacklabelops.crow.console.executor.IExecutor;
import com.blacklabelops.crow.console.executor.IExecutorTemplate;

public class MultiJobSchedulerIT {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Spy
	IScheduler scheduler = new JobScheduler();

	IDispatcher dispatcher = new Dispatcher();

	MultiJobScheduler multiScheduler;

	public Map<String, CountDownLatch> latch;

	@Before
	public void setup() {
		latch = new HashMap<>();
		multiScheduler = new MultiJobScheduler(scheduler, dispatcher);
	}

	@Test(timeout = 5000)
	public void whenJobThenExecuteSuccessfully() throws InterruptedException {
		ScheduledJob job1 = createJob(JobId.of("A"), 2);
		IExecutorTemplate executor = createExecutionTemplate("A");
		dispatcher.addJob(executor);
		scheduler.addJob(job1);
		Thread schedulerThread = new Thread(multiScheduler);
		schedulerThread.start();
		latch.get("A").await(3, TimeUnit.SECONDS);
		multiScheduler.stop();
		schedulerThread.join();
		assertEquals("Job must have been executed", 0, latch.get("A").getCount());
		verify(executor.createExecutor(), atLeastOnce()).run();
	}

	@Test(timeout = 5000)
	public void whenTwoJobsThenExecuteBoth() throws InterruptedException {
		ScheduledJob job1 = createJob(JobId.of("A"), 2);
		IExecutorTemplate executor = createExecutionTemplate("A");
		ScheduledJob job2 = createJob(JobId.of("B"), 3);
		IExecutorTemplate executor2 = createExecutionTemplate("B");
		dispatcher.addJob(executor);
		scheduler.addJob(job1);
		dispatcher.addJob(executor2);
		scheduler.addJob(job2);
		Thread schedulerThread = new Thread(multiScheduler);
		schedulerThread.start();
		latch.get("A").await(5, TimeUnit.SECONDS);
		latch.get("B").await(5, TimeUnit.SECONDS);
		multiScheduler.stop();
		schedulerThread.join();
		assertEquals("Job A must have been executed", 0, latch.get("A").getCount());
		assertEquals("Job B must have been executed", 0, latch.get("B").getCount());
	}

	@Test(timeout = 5000)
	public void whenTwoJobsSameTimesThenExecuteBothSequentially() throws InterruptedException {
		ScheduledJob job1 = createJob(JobId.of("A"), 2);
		IExecutorTemplate executor = createExecutionTemplate("A");
		ScheduledJob job2 = createJob(JobId.of("B"), 2);
		IExecutorTemplate executor2 = createExecutionTemplate("B");
		dispatcher.addJob(executor);
		scheduler.addJob(job1);
		dispatcher.addJob(executor2);
		scheduler.addJob(job2);
		Thread schedulerThread = new Thread(multiScheduler);
		schedulerThread.start();
		latch.get("A").await(5, TimeUnit.SECONDS);
		latch.get("B").await(5, TimeUnit.SECONDS);
		multiScheduler.stop();
		schedulerThread.join();
		assertEquals("Job A must have been executed", 0, latch.get("A").getCount());
		assertEquals("Job B must have been executed", 0, latch.get("B").getCount());
	}

	private ScheduledJob createJob(final JobId jobId, long secondsNextInvocation) {
		IExecutionTime executorTime = mock(IExecutionTime.class);
		when(executorTime.nextExecution(any())).thenAnswer(new Answer<ZonedDateTime>() {

			private long seconds = secondsNextInvocation;

			@Override
			public ZonedDateTime answer(InvocationOnMock invocationOnMock) throws Throwable {
				ZonedDateTime time = invocationOnMock.getArgument(0);
				return time.plusSeconds(seconds);
			}
		});
		return new ScheduledJob(jobId, executorTime);
	}

	private IExecutorTemplate createExecutionTemplate(final String name) {
		Job jobDefinition = Job.builder().id(name).name(name).build();
		latch.put(name, new CountDownLatch(1));
		IExecutor executor = mock(IExecutor.class);
		when(executor.getJobId()).thenReturn(jobDefinition.getId());
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				latch.get(name).countDown();
				return null;
			}
		}).when(executor).run();
		when(executor.getJobDefinition()).thenReturn(jobDefinition);
		IExecutorTemplate template = mock(IExecutorTemplate.class);
		when(template.createExecutor()).thenReturn(executor);
		when(template.getJobId()).thenReturn(jobDefinition.getId());
		return template;
	}
}
