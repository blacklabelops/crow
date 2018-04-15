package com.blacklabelops.crow.executor.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.logger.JobLogLogger;

public class SimpleConsoleUnixIntegrationIT {

    public ConsoleExecutor simpleConsole;

    public JobDefinition jobDefinition;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    AppenderSkeleton appender;

    @Captor
    ArgumentCaptor<LoggingEvent> logCaptor;

    @Before
    public void setup() {
        assert !System.getProperty("os.name").startsWith("Windows");
        jobDefinition = new JobDefinition();
        Logger.getRootLogger().addAppender(appender);
    }

    @After
    public void tearDown() {
        jobDefinition = null;
        Logger.getRootLogger().removeAppender(appender);
    }

    @Test
    public void whenNoDefinitionThenNullpointerException() {
        exception.expect(NullPointerException.class);
        simpleConsole = new ConsoleExecutor(null, null,null);
        simpleConsole.run();
    }

    @Test
    @Ignore
    public void whenEchoConsoleThenHelloOnLogs() {
        jobDefinition.setCommand("echo","hello world");
        jobDefinition.setJobName("echoJob");
        simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(new JobLogLogger("echoJob")).collect(Collectors.toList()));
        simpleConsole.run();
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        assertEquals("Log output must be info level!", Level.INFO, logCaptor.getValue().getLevel());
        assertEquals("Message must match echo directive message!","hello world",logCaptor.getValue().getMessage());
    }

    @Test
    public void whenEchoErrorConsoleThenErrorOnLogs() {
        jobDefinition.setCommand("/bin/bash","-c",">&2 echo error");
        jobDefinition.setJobName("errorJob");
        simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(new JobLogLogger("errorJob")).collect(Collectors.toList()));
        simpleConsole.run();
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        assertEquals("Log output must be error level!",Level.ERROR, logCaptor.getValue().getLevel());
        assertEquals("Must have same message as in echo directive!","error",logCaptor.getValue().getMessage());
    }
    
    @Test(timeout = 120000)
    public void testRun_WhenTimeOutDefined_ProcessTimedout() {
    		jobDefinition.setCommand("sleep","200");
    		jobDefinition.setTimeoutMinutes(1);
        jobDefinition.setJobName("echoJob");
        simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(new JobLogLogger("echoJob")).collect(Collectors.toList()));
        simpleConsole.run();
        assertTrue(simpleConsole.getExecutionResult().isTimedOut());
    }
    
    @Test(timeout = 5000)
    public void testRun_WhenTimeOutDefinedShortJob_JobIsNotTimedOut() {
    		jobDefinition.setCommand("sleep","2");
    		jobDefinition.setTimeoutMinutes(1);
        jobDefinition.setJobName("echoJob");
        simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(new JobLogLogger("echoJob")).collect(Collectors.toList()));
        simpleConsole.run();
        assertFalse(simpleConsole.getExecutionResult().isTimedOut());
        assertEquals(Integer.valueOf(0), simpleConsole.getExecutionResult().getReturnCode());
    }
    
    @Test
    @Ignore
    public void testRun_WhenWorkingDirectoryIsDefined_ExecuteInWorkDirectory() {
        String tempDirectory = System.getProperty("java.io.tmpdir");
        jobDefinition.setCommand("pwd");
        jobDefinition.setJobName("workdirJob");
        jobDefinition.setWorkingDir(tempDirectory);
        simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(new JobLogLogger("workdirJob")).collect(Collectors.toList()));
        simpleConsole.run();
        verify(appender).doAppend(logCaptor.capture());
        assertEquals("Must match working directory",tempDirectory,logCaptor.getValue().getMessage());
    }

    @Test
    @Ignore
    public void testRun_WhenPreCommandDefined_ExecutePreCommandsAndCommands() {
    		jobDefinition.setCommand("echo","hello world");
    		jobDefinition.setPreCommand("echo","hello preCommand");
    		jobDefinition.setPostCommand("echo","hello postCommand");
    		simpleConsole = new ConsoleExecutor(jobDefinition, null, Stream.of(new JobLogLogger("echoJob")).collect(Collectors.toList()));
        simpleConsole.run();
    }
}