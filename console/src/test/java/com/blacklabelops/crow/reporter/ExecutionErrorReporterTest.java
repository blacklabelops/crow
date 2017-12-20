package com.blacklabelops.crow.reporter;

import com.blacklabelops.crow.executor.IExecutor;
import com.blacklabelops.crow.scheduler.IScheduler;
import com.blacklabelops.crow.suite.FastTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Category(FastTests.class)
public class ExecutionErrorReporterTest {

    public ExecutionErrorReporter reporter;

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    IScheduler scheduler;

    @Mock
    IExecutor executor;

    @Before
    public void setup() {
        reporter = new ExecutionErrorReporter(scheduler);
    }

    @After
    public void tearDown() {
        reporter = null;
    }

    @Test
    public void testFailingJob_WhenFailing_ThenNotifyError() {
        reporter.failingJob(executor);
        verify(scheduler, times(1)).notifyExecutionError(eq(executor), nullable(Integer.class));
    }

    @Test
    public void testFailingJob_WhenFailingWithReturnCode_ThenNotifyErrorWithReturnCode() {
        Integer errorCode = Integer.valueOf(5);
        when(executor.getReturnCode()).thenReturn(errorCode);
        reporter.failingJob(executor);
        verify(scheduler, times(1)).notifyExecutionError(eq(executor), eq(errorCode));
    }


}