package com.blacklabelops.crow.scheduler;

import com.blacklabelops.crow.suite.FastTests;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by steffenbleul on 22.12.16.
 */
@Category(FastTests.class)
public class CronUtilsExecutionTimeTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void whenEmptyCronStringThenNullpointerException() {
        exception.expect(NullPointerException.class);
        CronUtilsExecutionTime cronHandler = new CronUtilsExecutionTime(null);
    }

    @Test
    public void whenIncorrectCronStringThenArgumentException() {
        exception.expect(IllegalArgumentException.class);
        CronUtilsExecutionTime cronHandler = new CronUtilsExecutionTime("qwd");
    }

 }
