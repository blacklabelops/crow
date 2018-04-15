package com.blacklabelops.crow.scheduler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.dispatcher.Dispatcher;
import com.blacklabelops.crow.dispatcher.IDispatcher;
import com.blacklabelops.crow.executor.IExecutorTemplate;
import com.blacklabelops.crow.executor.console.ConsoleExecutor;

public class MultiJobSchedulerIntegrationIT {

    public Map<String,CountDownLatch> latch;

    @Before
    public void setup() {
        latch = new HashMap<>();
    }

    @Test(timeout = 80000)
    public void whenJobByMinutwThenExecute() throws InterruptedException {
        IScheduler scheduler = new JobScheduler();
        IDispatcher dispatcher = new Dispatcher();
        MultiJobScheduler multischeduler = new MultiJobScheduler(scheduler,dispatcher);
        Job job1 = createJob("A","* * * * *");
        Job job2 = createJob("B","* * * * *");
        dispatcher.addJob(createExecutorTemplate("A",1));
        scheduler.addJob(job1);
        dispatcher.addJob(createExecutorTemplate("B",1));
        scheduler.addJob(job2);
        Thread schedulerThread = new Thread(multischeduler);
        schedulerThread.start();
        latch.get("A").await(65, TimeUnit.SECONDS);
        latch.get("B").await(65, TimeUnit.SECONDS);
        multischeduler.stop();
        schedulerThread.join();
        assertEquals("Job A must have been executed",0, latch.get("A").getCount());
        assertEquals("Job B must have been executed",0, latch.get("B").getCount());
    }

    @Test(timeout = 140000)
    public void whenTwoJobsByMinuteThenExecuteTwoTimesInTwoMinutes() throws InterruptedException {
        IScheduler scheduler = new JobScheduler();
        IDispatcher dispatcher = new Dispatcher();
        MultiJobScheduler multischeduler = new MultiJobScheduler(scheduler, dispatcher);
        Job job1 = createJob("A","* * * * *");
        Job job2 = createJob("B","* * * * *");
        dispatcher.addJob(createExecutorTemplate("A",2));
        scheduler.addJob(job1);
        dispatcher.addJob(createExecutorTemplate("B",2));
        scheduler.addJob(job2);
        Thread schedulerThread = new Thread(multischeduler);
        schedulerThread.start();
        latch.get("A").await(130, TimeUnit.SECONDS);
        latch.get("B").await(130, TimeUnit.SECONDS);
        multischeduler.stop();
        schedulerThread.join();
        assertEquals("Job A must have been executed",0, latch.get("A").getCount());
        assertEquals("Job B must have been executed",0, latch.get("B").getCount());
    }

    private Job createJob(final String name, String cronString) {
        JobDefinition defConsole = new JobDefinition();
        defConsole.setCommand("echo","Hello" + name);
        defConsole.setJobName(name);
        return new Job(name, new CronUtilsExecutionTime(cronString));
    }
    
    private IExecutorTemplate createExecutorTemplate(final String name, int latches) {
    		JobDefinition defConsole = new JobDefinition();
        defConsole.setCommand("echo","Hello" + name);
        defConsole.setJobName(name);
    		ConsoleExecutor console = new ConsoleExecutor(defConsole, null, null);
        latch.put(name,new CountDownLatch(latches));
        ConsoleExecutor spyConsole = spy(console);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                latch.get(name).countDown();
                return invocationOnMock.callRealMethod();
            }
        }).when(spyConsole).run();
    		IExecutorTemplate template = mock(IExecutorTemplate.class);
        when(template.createExecutor()).thenReturn(spyConsole);
        when(template.getJobName()).thenReturn(name);
        return template;
    }
}
