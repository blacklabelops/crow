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

import com.blacklabelops.crow.executor.ErrorMode;
import com.blacklabelops.crow.executor.ExecutionResult;
import com.blacklabelops.crow.executor.IExecutor;
import com.blacklabelops.crow.executor.IExecutorTemplate;

public class JobSchedulerUT {

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
        when(executor.getJobName()).thenReturn(name);
        IExecutionTime executorTime = mock(IExecutionTime.class);
        when(executorTime.nextExecution(any())).thenReturn(nextExecution);
        IExecutorTemplate template = mock(IExecutorTemplate.class);
        when(template.createExecutor()).thenReturn(executor);
        when(template.getJobName()).thenReturn(name);
        return new Job(template,executorTime);
    }

    @Test
    public void testNotifyFailingJob_WhenJobFaulty_UnscheduleJob() {
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        IExecutor executorJob = job.getExecutor();
        when(executorJob.getErrorMode()).thenReturn(ErrorMode.STOP);
        scheduler.addJob(job);
        scheduler.notifyFailingJob(executorJob, ExecutionResult.DROPPED_ALREADY_RUNNING);
        assertNull(scheduler.getNextExecutableJob());
    }

    @Test
    public void testNotifyFailingJob_WhenJobContinue_StillScheduleJob() {
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        IExecutor executorJob = job.getExecutor();
        when(executorJob.getErrorMode()).thenReturn(ErrorMode.CONTINUE);
        scheduler.addJob(job);
        scheduler.notifyFailingJob(executorJob, ExecutionResult.DROPPED_ALREADY_RUNNING);
        assertEquals(job, scheduler.getNextExecutableJob());
    }

    @Test
    public void testNotifyFailingJob_WhenNoErrorDefinition_StillScheduleJob() {
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        IExecutor executorJob = job.getExecutor();
        when(executorJob.getErrorMode()).thenReturn(null);
        scheduler.addJob(job);
        scheduler.notifyFailingJob(executorJob, ExecutionResult.DROPPED_ALREADY_RUNNING);
        assertEquals(job, scheduler.getNextExecutableJob());
    }

    @Test
    public void testNotifyExecutionError_WhenJobFaulty_UnscheduleJob() {
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        IExecutor executorJob = job.getExecutor();
        when(executorJob.getErrorMode()).thenReturn(ErrorMode.STOP);
        scheduler.addJob(job);
        scheduler.notifyExecutionError(executorJob, 1);
        assertNull(scheduler.getNextExecutableJob());
    }

    @Test
    public void testNotifyExecutionError_WhenJobContinue_StillScheduleJob() {
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        IExecutor executorJob = job.getExecutor();
        when(executorJob.getErrorMode()).thenReturn(ErrorMode.CONTINUE);
        scheduler.addJob(job);
        scheduler.notifyExecutionError(executorJob, 1);
        assertEquals(job, scheduler.getNextExecutableJob());
    }

    @Test
    public void testNotifyExecutionError_WhenNoErrorDefinition_StillScheduleJob() {
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        IExecutor executorJob = job.getExecutor();
        when(executorJob.getErrorMode()).thenReturn(null);
        scheduler.addJob(job);
        scheduler.notifyExecutionError(executorJob, 1);
        assertEquals(job, scheduler.getNextExecutableJob());
    }


}
