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
import com.blacklabelops.crow.console.definition.JobDefinition;
import com.blacklabelops.crow.console.dispatcher.Dispatcher;
import com.blacklabelops.crow.console.dispatcher.DispatcherResult;
import com.blacklabelops.crow.console.dispatcher.DispatchingResult;
import com.blacklabelops.crow.console.executor.IExecutor;

public class ExecutorPoolIT {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	public IExecutor executor;

	public CountDownLatch latch;

	public JobDefinition jobDefinition;

	@Before
	public void setup() {
		latch = new CountDownLatch(1);
		jobDefinition = new JobDefinition();
		jobDefinition.setJobName("A");
	}

	@Test
	public void whenNoExecutionThenNoCheckRunning() {
		Dispatcher pool = new Dispatcher();
		assertFalse(pool.checkRunning(executor));
	}

	@Test
	public void whenNewLongExecutionTheRunningTrue() {
		when(executor.getJobId()).thenReturn("A");
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
		when(executor.getJobId()).thenReturn("A");
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
		JobDefinition definition = new JobDefinition();
		definition.setJobName("A");
		definition.setExecutionMode(ExecutionMode.PARALLEL);
		when(executor.getJobDefinition()).thenReturn(definition);
		when(executor.getJobId()).thenReturn("A");
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
		JobDefinition definition = new JobDefinition();
		definition.setJobName("A");
		definition.setExecutionMode(ExecutionMode.SEQUENTIAL);
		when(executor.getJobDefinition()).thenReturn(definition);
		when(executor.getJobId()).thenReturn("A");
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
		DispatchingResult result = pool.addExecution(executor);
		synchLatch.countDown();
		synchLatch.await(5, TimeUnit.SECONDS);
		assertEquals(DispatcherResult.DROPPED_ALREADY_RUNNING, result.getDispatcherResult());
	}

	@Test
	public void testAddExecution_WhenNoMode_DefaultSequential_SecondExecutionWillBeDropped()
			throws InterruptedException {
		JobDefinition definition = new JobDefinition();
		definition.setJobName("A");
		when(executor.getJobDefinition()).thenReturn(definition);
		when(executor.getJobId()).thenReturn("A");
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
		DispatchingResult result = pool.addExecution(executor);
		synchLatch.countDown();
		synchLatch.await(5, TimeUnit.SECONDS);
		assertEquals(DispatcherResult.DROPPED_ALREADY_RUNNING, result.getDispatcherResult());
	}
}
