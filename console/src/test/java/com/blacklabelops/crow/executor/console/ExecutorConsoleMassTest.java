package com.blacklabelops.crow.executor.console;

import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.suite.SlowTests;
import com.blacklabelops.crow.util.FileAsserter;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

/**
 * Created by steffenbleul on 19.12.16.
 */
@Category(SlowTests.class)
public class ExecutorConsoleMassTest {

    public ExecutorConsole cli;

    public JobDefinition definition;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Rule
    public ExecutorConsoleRule executorRule = new ExecutorConsoleRule();

    @Rule
    public FileAsserter outputFile = new FileAsserter();

    @Rule
    public FileAsserter errorFile = new FileAsserter();

    @Before
    public void setup() {
        assert !System.getProperty("os.name").startsWith("Windows");
        cli = executorRule.getExecutor();
        definition = executorRule.getDefinition();
        cli.setOutputFile(outputFile.getFile());
        cli.setOutputErrorFile(errorFile.getFile());
    }

    @After
    public void tearDown() {
        cli = null;
        definition = null;
    }


    @Test
    public void whenTenCharsThenCorrectOutputfilesize() {
        int size = 10;
        String print = createPrintCommand(size);
        definition.setCommand("/bin/bash","-c",print);
        cli.execute(definition);
        Assert.assertEquals(size, outputFile.getFilesize());
    }

    private String createPrintCommand(int size) {
        String times = "{1.." + size + "}";
        return "printf '=%.0s' " + times;
    }

    @Test
    public void whenMillionharsThenCorrectOutputfilesize() {
        int size = 1000000;
        String print = createPrintCommand(size);
        definition.setCommand("/bin/bash","-c",print);
        cli.execute(definition);
        Assert.assertEquals(size, outputFile.getFilesize());
    }

}
