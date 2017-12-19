package com.blacklabelops.crow.application;

import com.blacklabelops.crow.config.Crow;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created by steffenbleul on 28.12.16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CrowDemon.class)
@TestPropertySource(locations="classpath:test.properties")
public class CrowConfigTest {

    @Autowired
    private Crow crow;

    @Test
    public void whenConfigLoadedThenCheckTests() {
        assertNotNull("Must have jobs!",crow.getJobs());
        assertEquals("Yaml must have two jobs!",2, crow.getJobs().size());
    }

    @Test
    public void whenFirstJobLoadedThenCorrectName() {
        assertEquals("Must match jobs name!","HelloWorld", crow.getJobs().get(0).getName());
    }

    @Test
    public void whenFirstJobLoadedThenCorrectCron() {
        assertEquals("Must match expected cron expression!","* * * * *",crow.getJobs().get(0).getCron());
    }

    @Test
    public void whenFirstJobLoadedThenCorrectCommand() {
        assertEquals("Must match command","echo 'Hello World!'",crow.getJobs().get(0).getCommand());
    }

    @Test
    public void whenSecondJobLoadedThenCorrectEnvironmentVariables() {
        assertEquals("Environment variable key must match!","MY_KEY",crow.getJobs().get(1).getEnvironments().keySet().stream().findFirst().orElse(""));
        assertEquals("Environment variable value must match!","myvalue",crow.getJobs().get(1).getEnvironments().values().stream().findFirst().orElse(""));
    }
}
