package com.blacklabelops.crow.executor;

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
public class ExecutorPoolTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    public IExecutor executor;

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
}
