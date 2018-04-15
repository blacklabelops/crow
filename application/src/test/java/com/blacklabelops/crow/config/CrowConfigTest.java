package com.blacklabelops.crow.config;

import com.blacklabelops.crow.application.CrowDemon;
import com.blacklabelops.crow.config.Crow;
import com.blacklabelops.crow.definition.ErrorMode;
import com.blacklabelops.crow.definition.ExecutionMode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;

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

    @Test
    public void testGetExecution_WhenNoExecutionDefined_ExecutionSequential() {
        String executionMode = crow.getJobs().get(0).getExecution();
        assertNull("Execution mode is not set in config",executionMode);
        assertEquals("Default execution mode must be sequential!", ExecutionMode.SEQUENTIAL, ExecutionMode.getMode(executionMode));
    }

    @Test
    public void testGetErrorMode_WhenNoErrorModenDefined_ErrorModeContinue() {
        String errorMode = crow.getJobs().get(0).getErrorMode();
        assertNull("Error mode must not be set in config", errorMode);
        assertEquals("Default error mode must be continuing!", ErrorMode.CONTINUE, ErrorMode.getMode(errorMode));
    }

    @Test
    public void testGetErrorMode_WhenContinueDefined_ErrorModeContinue() {
        String errorMode = crow.getJobs().get(1).getErrorMode();
        assertNotNull("Error mode must be set in config", errorMode);
        assertEquals("Default error mode must be continuing!", ErrorMode.CONTINUE, ErrorMode.getMode(errorMode));
    }
    
    @Test
    public void testGetTimeOutMinutes_WhenNoTimeoutDefined_TimeoutMustBeNull() {
    		Integer timeoutMinutes = crow.getJobs().get(1).getTimeOutMinutes();
    		assertNull("Timeout must be null in config", timeoutMinutes);
    }
    
    @Test
    public void testGetPreCommand_WhenNoPreCommandDefined_PreCommandMustBeNull() {
		String preCommand = crow.getJobs().get(1).getPreCommand();
		assertNull("Precommand must be null in config", preCommand);
    }
    
    @Test
    public void testGetPostCommand_WhenNoPostCommandDefined_PostCommandMustBeNull() {
		String postCommand = crow.getJobs().get(1).getPostCommand();
		assertNull("Postcommand must be null in config", postCommand);
    }
}
