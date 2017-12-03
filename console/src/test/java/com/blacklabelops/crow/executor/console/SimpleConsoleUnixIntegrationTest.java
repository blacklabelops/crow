package com.blacklabelops.crow.executor.console;

import com.blacklabelops.crow.executor.SimpleConsole;
import com.blacklabelops.crow.suite.SlowTests;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

/**
 * Created by steffenbleul on 21.12.16.
 */
@Category(SlowTests.class)
public class SimpleConsoleUnixIntegrationTest {


    public SimpleConsole simpleConsole;

    public DefinitionConsole definitionConsole;

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
        definitionConsole = new DefinitionConsole();
        Logger.getRootLogger().addAppender(appender);
    }

    @After
    public void tearDown() {
        definitionConsole = null;
        Logger.getRootLogger().removeAppender(appender);
    }

    @Test
    public void whenNoDefinitionThenExecutionException() {
        exception.expect(ExecutorException.class);
        simpleConsole = new SimpleConsole("test",null,new FileAccessor());
        simpleConsole.run();
    }

    @Test
    public void whenEchoConsoleThenHelloOnLogs() {
        definitionConsole.setCommand("echo","hello world");
        simpleConsole = new SimpleConsole("echoJob",definitionConsole, new FileAccessor());
        simpleConsole.run();
        verify(appender).doAppend(logCaptor.capture());
        assertEquals("Log output must be info level!",logCaptor.getValue().getLevel(), Level.INFO);
        assertEquals("Message must match echo directive message!","hello world",logCaptor.getValue().getMessage());
    }

    @Test
    public void whenEchoErrorConsoleThenErrorOnLogs() {
        definitionConsole.setCommand("/bin/bash","-c",">&2 echo error");
        simpleConsole = new SimpleConsole("errorJob",definitionConsole, new FileAccessor());
        simpleConsole.run();
        verify(appender).doAppend(logCaptor.capture());
        assertEquals("Log output must be error level!",logCaptor.getValue().getLevel(), Level.ERROR);
        assertEquals("Must have same message as in echo directive!","error",logCaptor.getValue().getMessage());
    }


}