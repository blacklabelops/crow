package com.blacklabelops.crow.scheduler;

import com.blacklabelops.crow.executor.SimpleConsole;
import com.blacklabelops.crow.executor.console.DefinitionConsole;
import com.blacklabelops.crow.executor.console.FileAccessor;
import com.blacklabelops.crow.suite.SlowTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

/**
 * Created by steffenbleul on 28.12.16.
 */
@Category(SlowTests.class)
public class MultiJobSchedulerIntegrationTest {

    public Map<String,CountDownLatch> latch;

    @Before
    public void setup() {
        latch = new HashMap<>();
    }

    @Test(timeout = 70000)
    public void whenJobByMinutwThenExecute() throws InterruptedException {
        IScheduler scheduler = new JobScheduler();
        MultiJobScheduler multischeduler = new MultiJobScheduler(scheduler);
        Job job1 = createJob("A","* * * * *",1);
        Job job2 = createJob("B","* * * * *",1);
        scheduler.addJob(job1);
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
        MultiJobScheduler multischeduler = new MultiJobScheduler(scheduler);
        Job job1 = createJob("A","* * * * *",2);
        Job job2 = createJob("B","* * * * *",2);
        scheduler.addJob(job1);
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

    private Job createJob(final String name, String cronString,int latches) {
        DefinitionConsole defConsole = new DefinitionConsole();
        defConsole.setCommand("echo","Hello" + name);
        SimpleConsole console = new SimpleConsole(name, defConsole,new FileAccessor());
        latch.put(name,new CountDownLatch(latches));
        SimpleConsole spyConsole = spy(console);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                latch.get(name).countDown();
                console.run();
                return null;
            }
        }).when(spyConsole).run();
        return new Job(spyConsole, new CronUtilsExecutionTime(cronString));
    }
}
