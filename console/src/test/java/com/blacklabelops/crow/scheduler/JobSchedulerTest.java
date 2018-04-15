package com.blacklabelops.crow.scheduler;

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

import com.blacklabelops.crow.definition.ErrorMode;
import com.blacklabelops.crow.dispatcher.DispatcherResult;
import com.blacklabelops.crow.executor.IExecutor;

public class JobSchedulerTest {

    public JobScheduler scheduler = new JobScheduler();

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    public IExecutor executor;

    @Test
    public void whenFirstEntryTheNextThenReturnFirstJob() {
        ZonedDateTime dateTime1 = ZonedDateTime.now().plusMinutes(1);
        Job job1 = createJob("A",dateTime1);
        ZonedDateTime dateTime2 = ZonedDateTime.now().plusMinutes(5);
        Job job2 = createJob("B",dateTime2);
        JobScheduler scheduler = new JobScheduler();
        scheduler.addJob(job1);
        scheduler.addJob(job2);
        Job nextExecutableJob = scheduler.getNextExecutableJob();
        assertEquals(job1,nextExecutableJob);
    }

    @Test
    public void whenSecondEntryTheNextThenReturnSecondJob() {
        ZonedDateTime dateTime1 = ZonedDateTime.now().plusMinutes(5);
        Job job1 = createJob("A",dateTime1);
        ZonedDateTime dateTime2 = ZonedDateTime.now().plusMinutes(1);
        Job job2 = createJob("B",dateTime2);
        JobScheduler scheduler = new JobScheduler();
        scheduler.addJob(job1);
        scheduler.addJob(job2);
        Job nextExecutableJob = scheduler.getNextExecutableJob();
        assertEquals(job2,nextExecutableJob);
    }
    
    private Job createJob(String name, ZonedDateTime nextExecution) {
        IExecutionTime executorTime = mock(IExecutionTime.class);
        when(executorTime.nextExecution(any())).thenReturn(nextExecution);
        return new Job(name,executorTime);
    }

    @Test
    public void testNotifyFailingJob_WhenJobFaulty_UnscheduleJob() {
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        when(executor.getErrorMode()).thenReturn(ErrorMode.STOP);
        when(executor.getJobName()).thenReturn("A");
        scheduler.addJob(job);
        scheduler.notifyFailingJob(executor, DispatcherResult.DROPPED_ALREADY_RUNNING);
        assertNull(scheduler.getNextExecutableJob());
    }

    @Test
    public void testNotifyFailingJob_WhenJobContinue_StillScheduleJob() {
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        when(executor.getErrorMode()).thenReturn(ErrorMode.CONTINUE);
        when(executor.getJobName()).thenReturn("A");
        scheduler.addJob(job);
        scheduler.notifyFailingJob(executor, DispatcherResult.DROPPED_ALREADY_RUNNING);
        assertEquals(job, scheduler.getNextExecutableJob());
    }

    @Test
    public void testNotifyFailingJob_WhenNoErrorDefinition_StillScheduleJob() {
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        when(executor.getErrorMode()).thenReturn(null);
        when(executor.getJobName()).thenReturn("A");
        scheduler.addJob(job);
        scheduler.notifyFailingJob(executor, DispatcherResult.DROPPED_ALREADY_RUNNING);
        assertEquals(job, scheduler.getNextExecutableJob());
    }

    @Test
    public void testNotifyExecutionError_WhenJobFaulty_UnscheduleJob() {
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        when(executor.getErrorMode()).thenReturn(ErrorMode.STOP);
        when(executor.getJobName()).thenReturn("A");
        scheduler.addJob(job);
        scheduler.notifyExecutionError(executor, 1);
        assertNull(scheduler.getNextExecutableJob());
    }

    @Test
    public void testNotifyExecutionError_WhenJobContinue_StillScheduleJob() {
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        when(executor.getJobName()).thenReturn("A");
        when(executor.getErrorMode()).thenReturn(ErrorMode.CONTINUE);
        scheduler.addJob(job);
        scheduler.notifyExecutionError(executor, 1);
        assertEquals(job, scheduler.getNextExecutableJob());
    }

    @Test
    public void testNotifyExecutionError_WhenNoErrorDefinition_StillScheduleJob() {
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        when(executor.getJobName()).thenReturn("A");
        when(executor.getErrorMode()).thenReturn(null);
        scheduler.addJob(job);
        scheduler.notifyExecutionError(executor, 1);
        assertEquals(job, scheduler.getNextExecutableJob());
    }


}
