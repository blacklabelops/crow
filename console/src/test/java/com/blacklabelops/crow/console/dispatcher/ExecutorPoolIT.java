package com.blacklabelops.crow.console.dispatcher;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import com.blacklabelops.crow.console.definition.ExecutionMode;
import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.definition.JobId;
import com.blacklabelops.crow.console.executor.IExecutor;

public class ExecutorPoolIT {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	public IExecutor executor;

	public CountDownLatch latch;

	public Job jobDefinition;

	@Before
	public void setup() {
		latch = new CountDownLatch(1);
		jobDefinition = Job.builder().name("A").id(JobId.builder().id("A").build()).build();
	}

	@Test
	public void whenNoExecutionThenNoCheckRunning() {
		Dispatcher pool = new Dispatcher();
		assertFalse(pool.checkRunning(executor));
	}

	@Test
	public void whenNewLongExecutionTheRunningTrue() {
		when(executor.getJobId()).thenReturn(JobId.builder().id("A").build());
		when(executor.getJobDefinition()).thenReturn(jobDefinition);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				Thread.sleep(1000);
				return null;
			}
		}).when(executor).run();
		Dispatcher pool = new Dispatcher();
		pool.addExecution(executor);
		assertTrue(pool.checkRunning(executor));
	}

	@Test
	public void whenExecutionFinishedThenRunningFalse() throws InterruptedException {
		when(executor.getJobId()).thenReturn(JobId.builder().id("A").build());
		when(executor.getJobDefinition()).thenReturn(jobDefinition);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				Thread.sleep(1000);
				latch.countDown();
				return null;
			}
		}).when(executor).run();
		Dispatcher pool = new Dispatcher();
		pool.addExecution(executor);
		latch.await(3, TimeUnit.SECONDS);
		assertEquals(0, latch.getCount());
		Assert.assertFalse(pool.checkRunning(executor));
	}

	@Test
	public void testAddExecution_WhenModeParallel_NoDropOfSecondExecution() throws InterruptedException {
		Job definition = Job.builder().id(JobId.builder().id("A").build()).name("A")
				.executorMode(
						ExecutionMode.PARALLEL).build();
		when(executor.getJobDefinition()).thenReturn(definition);
		when(executor.getJobId()).thenReturn(JobId.builder().id("A").build());
		CountDownLatch synchLatch = new CountDownLatch(2);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				synchLatch.countDown();
				synchLatch.await();
				return null;
			}
		}).when(executor).run();
		Dispatcher pool = new Dispatcher();
		pool.addExecution(executor);
		pool.addExecution(executor);
		synchLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, synchLatch.getCount());
	}

	@Test
	public void testAddExecution_WhenModeSequential_SecondExecutionWillBeDropped() throws InterruptedException {
		Job definition = Job.builder().id(JobId.builder().id("A").build()).name("A")
				.executorMode(
						ExecutionMode.SEQUENTIAL)
				.build();
		when(executor.getJobDefinition()).thenReturn(definition);
		when(executor.getJobId()).thenReturn(JobId.builder().id("A").build());
		CountDownLatch synchLatch = new CountDownLatch(2);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				synchLatch.countDown();
				synchLatch.await();
				return null;
			}
		}).when(executor).run();
		Dispatcher pool = new Dispatcher();
		pool.addExecution(executor);
		AbstractDispatchingResult result = pool.addExecution(executor);
		synchLatch.countDown();
		synchLatch.await(5, TimeUnit.SECONDS);
		assertEquals(DispatcherResult.DROPPED_ALREADY_RUNNING, result.getDispatcherResult());
	}

	@Test
	public void testAddExecution_WhenNoMode_DefaultSequential_SecondExecutionWillBeDropped()
			throws InterruptedException {
		Job definition = Job.builder().id(JobId.builder().id("A").build()).name("A").build();
		when(executor.getJobDefinition()).thenReturn(definition);
		when(executor.getJobId()).thenReturn(JobId.builder().id("A").build());
		CountDownLatch synchLatch = new CountDownLatch(2);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				synchLatch.countDown();
				synchLatch.await();
				return null;
			}
		}).when(executor).run();
		Dispatcher pool = new Dispatcher();
		pool.addExecution(executor);
		AbstractDispatchingResult result = pool.addExecution(executor);
		synchLatch.countDown();
		synchLatch.await(5, TimeUnit.SECONDS);
		assertEquals(DispatcherResult.DROPPED_ALREADY_RUNNING, result.getDispatcherResult());
	}
}
