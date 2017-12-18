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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by steffenbleul on 22.12.16.
 */
@Category(SlowTests.class)
public class SingleJobSchedulerIntegrationTest {

    public CountDownLatch latch;

    @Before
    public void setup() {
        latch = new CountDownLatch(1);
    }

    @Test(timeout = 70000)
    public void whenJobBySecondThenExecute() throws InterruptedException {
        String joName = "EchoJob";
        DefinitionConsole definition = new DefinitionConsole();
        definition.setCommand("echo","hello");
        definition.setJobName(joName);
        SimpleConsole executor = new SimpleConsole(definition, null, null);
        SimpleConsole spyExecutor = spy(executor);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                latch.countDown();
                executor.run();
                return null;
            }
        }).when(spyExecutor).run();
        CronUtilsExecutionTime cron = new CronUtilsExecutionTime("* * * * *");
        CronUtilsExecutionTime spyCron = spy(cron);
        SingleJobScheduler scheduler = new SingleJobScheduler(spyExecutor,spyCron);
        Thread schedulerThread = new Thread(scheduler);
        schedulerThread.start();
        latch.await(65, TimeUnit.SECONDS);
        scheduler.stop();
        schedulerThread.join();
        assertEquals(0,latch.getCount());
        verify(spyCron,atLeastOnce()).nextExecution(any());
        verify(spyExecutor,atLeastOnce()).run();
    }
}
