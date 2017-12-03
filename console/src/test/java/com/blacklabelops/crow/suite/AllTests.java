package com.blacklabelops.crow.suite;

import com.blacklabelops.crow.executor.console.ExecutorConsoleMassTest;
import com.blacklabelops.crow.executor.console.ExecutorConsoleUnixIntegrationTest;
import com.blacklabelops.crow.executor.console.SimpleConsoleUnixIntegrationTest;
import com.blacklabelops.crow.scheduler.*;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by steffenbleul on 23.12.16.
 */
@RunWith(Categories.class)
@Suite.SuiteClasses( { ExecutorConsoleMassTest.class,
        ExecutorConsoleUnixIntegrationTest.class,
        SimpleConsoleUnixIntegrationTest.class,
        CronUtilsExecutionTimeTest.class,
        JobSchedulerUnitTest.class,
        SingleJobSchedulerIntegrationTest.class,
        SingleJobSchedulerTest.class,
        MultiJobSchedulerTest.class,
        MultiJobSchedulerIntegrationTest.class
})
public class AllTests {
}
