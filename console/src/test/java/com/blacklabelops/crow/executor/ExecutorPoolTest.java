package com.blacklabelops.crow.executor;

import com.blacklabelops.crow.suite.FastTests;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Created by steffenbleul on 23.12.16.
 */
@Category(FastTests.class)
public class ExecutorPoolTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    public IExecutor executor;

    @Mock
    public IExecutor executorB;

    public CountDownLatch latch;

    @Before
    public void setup() {
        latch = new CountDownLatch(1);
    }

    @Test
    public void whenNoExecutionThenNoCheckRunning() {
        ExecutorPool pool = new ExecutorPool();
        assertFalse(pool.checkRunning(executor));
    }

    @Test
    public void whenNewLongExecutionTheRunningTrue() {
        when(executor.getJobName()).thenReturn("A");
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                Thread.sleep(1000);
                return null;
            }
        }).when(executor).run();
        ExecutorPool pool = new ExecutorPool();
        pool.addExecution(executor);
        assertTrue(pool.checkRunning(executor));
    }

    @Test
    public void whenExecutionFinishedThenRunningFalse() throws InterruptedException {
        when(executor.getJobName()).thenReturn("A");
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                Thread.sleep(1000);
                latch.countDown();
                return null;
            }
        }).when(executor).run();
        ExecutorPool pool = new ExecutorPool();
        pool.addExecution(executor);
        latch.await(3, TimeUnit.SECONDS);
        assertEquals(0, latch.getCount());
        Assert.assertFalse(pool.checkRunning(executor));
    }

    @Test
    public void testAddExecution_WhenModeParallel_NoDropOfSecondExecution() throws InterruptedException {
        when(executor.getJobName()).thenReturn("A");
        when(executor.getExecutionMode()).thenReturn(ExecutionMode.PARALLEL);
        CountDownLatch synchLatch = new CountDownLatch(2);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                synchLatch.countDown();
                synchLatch.await();
                return null;
            }
        }).when(executor).run();
        ExecutorPool pool = new ExecutorPool();
        pool.addExecution(executor);
        pool.addExecution(executor);
        synchLatch.await(5, TimeUnit.SECONDS);
        assertEquals(0, synchLatch.getCount());
    }

    @Test
    public void testAddExecution_WhenModeSequential_SecondExecutionWillBeDropped() throws InterruptedException {
        when(executor.getJobName()).thenReturn("A");
        when(executor.getExecutionMode()).thenReturn(ExecutionMode.SEQUENTIAL);
        CountDownLatch synchLatch = new CountDownLatch(2);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                synchLatch.countDown();
                synchLatch.await();
                return null;
            }
        }).when(executor).run();
        ExecutorPool pool = new ExecutorPool();
        pool.addExecution(executor);
        ExecutionResult result = pool.addExecution(executor);
        synchLatch.countDown();
        synchLatch.await(5, TimeUnit.SECONDS);
        assertEquals(ExecutionResult.DROPPED_ALREADY_RUNNING, result);
    }

    @Test
    public void testAddExecution_WhenNoMode_DefaultSequential_SecondExecutionWillBeDropped() throws InterruptedException {
        when(executor.getJobName()).thenReturn("A");
        when(executor.getExecutionMode()).thenReturn(null);
        CountDownLatch synchLatch = new CountDownLatch(2);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                synchLatch.countDown();
                synchLatch.await();
                return null;
            }
        }).when(executor).run();
        ExecutorPool pool = new ExecutorPool();
        pool.addExecution(executor);
        ExecutionResult result = pool.addExecution(executor);
        synchLatch.countDown();
        synchLatch.await(5, TimeUnit.SECONDS);
        assertEquals(ExecutionResult.DROPPED_ALREADY_RUNNING, result);
    }
}
