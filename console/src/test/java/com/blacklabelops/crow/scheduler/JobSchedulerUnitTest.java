package com.blacklabelops.crow.scheduler;

import com.blacklabelops.crow.executor.IExecutor;
import org.junit.Test;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by steffenbleul on 22.12.16.
 */
public class JobSchedulerUnitTest {

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
        IExecutor executor = mock(IExecutor.class);
        when(executor.getJobName()).thenReturn(name);
        IExecutionTime executorTime = mock(IExecutionTime.class);
        when(executorTime.nextExecution(any())).thenReturn(nextExecution);
        return new Job(executor,executorTime);
    }
}
