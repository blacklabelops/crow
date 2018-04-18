package com.blacklabelops.crow.console.scheduler;

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

import com.blacklabelops.crow.console.definition.ErrorMode;
import com.blacklabelops.crow.console.definition.JobDefinition;
import com.blacklabelops.crow.console.dispatcher.DispatcherResult;
import com.blacklabelops.crow.console.dispatcher.DispatchingResult;
import com.blacklabelops.crow.console.executor.ExecutionResult;
import com.blacklabelops.crow.console.executor.IExecutor;
import com.blacklabelops.crow.console.scheduler.IExecutionTime;
import com.blacklabelops.crow.console.scheduler.Job;
import com.blacklabelops.crow.console.scheduler.JobScheduler;

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
        scheduler.addJob(job);
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setJobName("A");
        jobDefinition.setErrorMode(ErrorMode.STOP);
        DispatchingResult result = new DispatchingResult(jobDefinition);
        result.setDispatcherResult(DispatcherResult.DROPPED_ALREADY_RUNNING);
        
        scheduler.notifyDispatchingError(result);
        
        assertNull(scheduler.getNextExecutableJob());
    }

    @Test
    public void testNotifyFailingJob_WhenJobContinue_StillScheduleJob() {
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        scheduler.addJob(job);
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setJobName("A");
        jobDefinition.setErrorMode(ErrorMode.CONTINUE);
        DispatchingResult result = new DispatchingResult(jobDefinition);
        result.setDispatcherResult(DispatcherResult.DROPPED_ALREADY_RUNNING);
        
        scheduler.notifyDispatchingError(result);
        
        assertEquals(job, scheduler.getNextExecutableJob());
    }

    @Test
    public void testNotifyFailingJob_WhenNoErrorDefinition_StillScheduleJob() {
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        scheduler.addJob(job);
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setJobName("A");
        DispatchingResult result = new DispatchingResult(jobDefinition);
        result.setDispatcherResult(DispatcherResult.DROPPED_ALREADY_RUNNING);
        
        scheduler.notifyDispatchingError(result);
        
        assertEquals(job, scheduler.getNextExecutableJob());
    }

    @Test
    public void testNotifyExecutionError_WhenJobFaulty_UnscheduleJob() {
	    	JobDefinition jobDefinition = new JobDefinition();
	    	jobDefinition.setJobName("A");
	    	jobDefinition.setErrorMode(ErrorMode.STOP);
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        scheduler.addJob(job);
        ExecutionResult result = new ExecutionResult(jobDefinition);
        
        scheduler.notifyExecutionError(result);
        
        assertNull(scheduler.getNextExecutableJob());
    }

    @Test
    public void testNotifyExecutionError_WhenJobContinue_StillScheduleJob() {
	    	JobDefinition jobDefinition = new JobDefinition();
	    	jobDefinition.setJobName("A");
	    	jobDefinition.setErrorMode(ErrorMode.CONTINUE);
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        scheduler.addJob(job);
        ExecutionResult result = new ExecutionResult(jobDefinition);
        
        scheduler.notifyExecutionError(result);
        
        assertEquals(job, scheduler.getNextExecutableJob());
    }

    @Test
    public void testNotifyExecutionError_WhenNoErrorDefinition_StillScheduleJob() {
    		JobDefinition jobDefinition = new JobDefinition();
    		jobDefinition.setJobName("A");
        Job job = createJob("A",ZonedDateTime.now().plusMinutes(1));
        scheduler.addJob(job);
        ExecutionResult result = new ExecutionResult(jobDefinition);
        
        scheduler.notifyExecutionError(result);
        
        assertEquals(job, scheduler.getNextExecutableJob());
    }


}
