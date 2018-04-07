package com.blacklabelops.crow.scheduler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CronUtilsExecutionTimeUT {

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
