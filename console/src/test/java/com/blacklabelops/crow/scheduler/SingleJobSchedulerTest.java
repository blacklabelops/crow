package com.blacklabelops.crow.scheduler;

import com.blacklabelops.crow.executor.IExecutor;
import com.blacklabelops.crow.suite.SlowTests;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by steffenbleul on 22.12.16.
 */
@Category(SlowTests.class)
public class SingleJobSchedulerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    IExecutor executor;

    @Mock
    IExecutionTime executionTime;

    @InjectMocks
    SingleJobScheduler scheduler;

    public CountDownLatch latch;

    @Before
    public void setup() {
        latch = new CountDownLatch(1);
    }

    @Test(timeout = 5000)
    public void whenDefinitionByZeroThenExecuteOnce() throws InterruptedException {
        when(executionTime.nextExecution(any())).thenReturn(ZonedDateTime.now());
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                latch.countDown();
                return null;
            }
        }).when(executor).run();
        Thread schedulerThread = new Thread(scheduler);
        schedulerThread.start();
        latch.await(1, TimeUnit.SECONDS);
        scheduler.stop();
        schedulerThread.join();
        verify(executor, atLeastOnce()).run();
    }

    @Test(timeout = 10000)
    public void whenDefinitionByMinuteThenExecuteOnce() throws InterruptedException {
        when(executionTime.nextExecution(any())).thenReturn(ZonedDateTime.now().plusSeconds(5));
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                latch.countDown();
                return null;
            }
        }).when(executor).run();
        Thread schedulerThread = new Thread(scheduler);
        schedulerThread.start();
        latch.await(6, TimeUnit.SECONDS);
        scheduler.stop();
        schedulerThread.join();
        verify(executor, atLeastOnce()).run();
    }

    @Test(timeout = 10000)
    public void whenStopSignalThenExitBeforeExecution() throws InterruptedException {
        when(executionTime.nextExecution(any())).thenReturn(ZonedDateTime.now().plusSeconds(5));
        Thread schedulerThread = new Thread(scheduler);
        schedulerThread.start();
        scheduler.stop();
        schedulerThread.join();
        verify(executor, never()).run();
    }

}
