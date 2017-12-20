package com.blacklabelops.crow.reporter;

import com.blacklabelops.crow.executor.IExecutor;
import com.blacklabelops.crow.suite.FastTests;
import org.apache.log4j.AppenderSkeleton;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.time.LocalDateTime;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@Category(FastTests.class)
public class ConsoleReporterTest {

    public ConsoleReporter reporter = new ConsoleReporter();

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    IExecutor executor;

    @Test
    public void testStartingJob() {
        when(executor.getJobName()).thenReturn("Name");
        when(executor.getStartingTime()).thenReturn(LocalDateTime.now());
        reporter.startingJob(executor);
    }

    @Test
    public void finishedJob() {
        when(executor.getJobName()).thenReturn("Name");
        when(executor.getFinishingTime()).thenReturn(LocalDateTime.now());
        when(executor.getReturnCode()).thenReturn(Integer.valueOf(5));
        reporter.finishedJob(executor);
    }


}