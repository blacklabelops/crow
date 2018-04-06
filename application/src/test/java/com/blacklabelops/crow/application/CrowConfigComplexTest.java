package com.blacklabelops.crow.application;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.blacklabelops.crow.config.Crow;
import com.blacklabelops.crow.executor.ErrorMode;
import com.blacklabelops.crow.executor.ExecutionMode;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CrowDemon.class)
@TestPropertySource(locations="classpath:testComplex.properties")
public class CrowConfigComplexTest {

    @Autowired
    private Crow crow;

    @Test
    public void testGetExecution_ConfigSetToParallel_ParallelExecutionMode() {
        String executionMode = crow.getJobs().get(0).getExecution();
        assertEquals("Config set to parallel", ExecutionMode.PARALLEL.toString().toLowerCase(), executionMode);
        assertEquals("Execution mode must result in parallel!", ExecutionMode.PARALLEL, ExecutionMode.getMode(executionMode));
    }

    @Test
    public void testGetErrorMode_WhenErrorModeStop_ThenErrorModeStop() {
        String errorMode = crow.getJobs().get(0).getErrorMode();
        assertEquals("Config set to stop", ErrorMode.STOP.toString().toLowerCase(), errorMode);
        assertEquals("Error mode must result in stop!", ErrorMode.STOP, ErrorMode.getMode(errorMode));
    }

    @Test
    public void testGetShellCommand_WhenSet_ThenRead() {
        String shellCommand = crow.getJobs().get(0).getShellCommand();
        assertNotNull("Shellcommand must be set!", shellCommand);
        assertEquals("Should be bash command!","/bin/bash -c", shellCommand);
    }

    @Test
    public void testGetWorkingDirectory_WhenSet_ThenRead() {
        String workingDirectory = crow.getJobs().get(0).getWorkingDirectory();
        assertNotNull("Working directory must be set!", workingDirectory);
        assertEquals("Should be temp directory!","/tmp", workingDirectory);
    }
    
    @Test
    public void testGetTimeOutMinutes_WhenNoTimeoutDefined_TimeoutMustBeNull() {
    		Integer timeoutMinutes = crow.getJobs().get(0).getTimeOutMinutes();
    		assertNotNull("Timeout must be found in config", timeoutMinutes);
    		assertEquals("Should be defined timeout!",Integer.valueOf(5), timeoutMinutes);
    }
    
    @Test
    public void testGetPreCommand_WhenNoPreCommandDefined_PreCommandMustBeNull() {
		String preCommand = crow.getJobs().get(0).getPreCommand();
		assertNotNull("Precommand must be found in config", preCommand);
		assertEquals("Should be defined PreCommand!","echo PreCommand", preCommand);
    }
    
    @Test
    public void testGetPostCommand_WhenNoPostCommandDefined_PostCommandMustBeNull() {
		String postCommand = crow.getJobs().get(0).getPostCommand();
		assertNotNull("Postcommand must be found in config", postCommand);
		assertEquals("Should be defined PostCommand!","echo PostCommand", postCommand);
    }
}
