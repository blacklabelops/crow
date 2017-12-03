package com.blacklabelops.crow.application;

import com.blacklabelops.crow.demon.SchedulerDemon;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
@TestPropertySource(locations="classpath:test.properties")
public class SchedulerDemonTry {

    @Autowired
    public SchedulerDemon demon;

    @Test
    public void whenSuccessfullyInitializedThenNotNull() {
        assertNotNull(demon);
    }


}
