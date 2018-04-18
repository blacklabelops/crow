package com.blacklabelops.crow.console.scheduler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blacklabelops.crow.console.scheduler.CronUtilsExecutionTime;

public class CronUtilsExecutionTimeTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void whenEmptyCronStringThenNullpointerException() {
        exception.expect(NullPointerException.class);
        new CronUtilsExecutionTime(null);
    }

    @Test
    public void whenIncorrectCronStringThenArgumentException() {
        exception.expect(IllegalArgumentException.class);
        new CronUtilsExecutionTime("qwd");
    }

 }
