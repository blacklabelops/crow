package com.blacklabelops.crow.console.scheduler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.dispatcher.Dispatcher;
import com.blacklabelops.crow.console.dispatcher.IDispatcher;
import com.blacklabelops.crow.console.executor.IExecutorTemplate;
import com.blacklabelops.crow.console.executor.console.ConsoleExecutor;
import com.blacklabelops.crow.console.logger.IJobLogger;
import com.blacklabelops.crow.console.logger.JobLogLogger;

public class MultiJobSchedulerIntegrationIT {

	public Map<String, CountDownLatch> latch;

	@Before
	public void setup() {
		latch = new HashMap<>();
	}

	@Test(timeout = 80000)
	public void whenJobByMinutwThenExecute() throws InterruptedException {
		IScheduler scheduler = new JobScheduler();
		IDispatcher dispatcher = new Dispatcher();
		MultiJobScheduler multischeduler = new MultiJobScheduler(scheduler, dispatcher);
		ScheduledJob job1 = createJob("A", "* * * * *");
		ScheduledJob job2 = createJob("B", "* * * * *");
		dispatcher.addJob(createExecutorTemplate("A", 1));
		scheduler.addJob(job1);
		dispatcher.addJob(createExecutorTemplate("B", 1));
		scheduler.addJob(job2);
		Thread schedulerThread = new Thread(multischeduler);
		schedulerThread.start();
		latch.get("A").await(65, TimeUnit.SECONDS);
		latch.get("B").await(65, TimeUnit.SECONDS);
		multischeduler.stop();
		schedulerThread.join();
		assertEquals("Job A must have been executed", 0, latch.get("A").getCount());
		assertEquals("Job B must have been executed", 0, latch.get("B").getCount());
	}

	@Test(timeout = 140000)
	public void whenTwoJobsByMinuteThenExecuteTwoTimesInTwoMinutes() throws InterruptedException {
		IScheduler scheduler = new JobScheduler();
		IDispatcher dispatcher = new Dispatcher();
		MultiJobScheduler multischeduler = new MultiJobScheduler(scheduler, dispatcher);
		ScheduledJob job1 = createJob("A", "* * * * *");
		ScheduledJob job2 = createJob("B", "* * * * *");
		dispatcher.addJob(createExecutorTemplate("A", 2));
		scheduler.addJob(job1);
		dispatcher.addJob(createExecutorTemplate("B", 2));
		scheduler.addJob(job2);
		Thread schedulerThread = new Thread(multischeduler);
		schedulerThread.start();
		latch.get("A").await(130, TimeUnit.SECONDS);
		latch.get("B").await(130, TimeUnit.SECONDS);
		multischeduler.stop();
		schedulerThread.join();
		assertEquals("Job A must have been executed", 0, latch.get("A").getCount());
		assertEquals("Job B must have been executed", 0, latch.get("B").getCount());
	}

	private ScheduledJob createJob(final String name, String cronString) {
		Job defConsole = Job.builder().id(name).name(name).command(Arrays.asList("echo", "Hello" + name)).build();
		return new ScheduledJob(defConsole.getId(), new CronUtilsExecutionTime(cronString));
	}

	private IExecutorTemplate createExecutorTemplate(final String name, int latches) {
		List<IJobLogger> loggers = new ArrayList<>();
		loggers.add(new JobLogLogger(name));
		Job defConsole = Job.builder().id(name).name(name).command(Arrays.asList("echo", "Hello" + name)).build();
		ConsoleExecutor console = new ConsoleExecutor(defConsole, null, loggers);
		latch.put(name, new CountDownLatch(latches));
		ConsoleExecutor spyConsole = spy(console);
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				latch.get(name).countDown();
				return invocationOnMock.callRealMethod();
			}
		}).when(spyConsole).run();
		IExecutorTemplate template = mock(IExecutorTemplate.class);
		when(template.createExecutor()).thenReturn(spyConsole);
		when(template.getJobId()).thenReturn(defConsole.getId());
		return template;
	}
}
