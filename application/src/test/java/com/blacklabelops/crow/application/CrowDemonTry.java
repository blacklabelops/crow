package com.blacklabelops.crow.application;

import com.blacklabelops.crow.demon.SchedulerDemon;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

/**
 * Created by steffenbleul on 29.12.16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CrowDemon.class)
@TestPropertySource(locations="classpath:tryOut.properties")
public class CrowDemonTry {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CrowDemon demon;

    @Autowired
    SchedulerDemon scheduler;

    @Test
    public void testInstance() {

    }

    @Test(timeout = 70000)
    public void testRunning() throws InterruptedException {
        Thread.sleep(65000);
        ((ConfigurableApplicationContext)demon).close();
    }
}
