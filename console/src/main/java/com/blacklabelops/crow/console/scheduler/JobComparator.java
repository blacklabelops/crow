package com.blacklabelops.crow.console.scheduler;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;


public class JobComparator implements Comparator<ScheduledJob> {

    private ChronoUnit chronoUnit = ChronoUnit.MILLIS;

    @Override
    public int compare(ScheduledJob source, ScheduledJob target) {
        ZonedDateTime nextExecutionSource = source.getNextExecution();
        ZonedDateTime nextExecutionTarget = target.getNextExecution();
        return Long.valueOf(chronoUnit.between(nextExecutionSource,nextExecutionTarget)).intValue();
    }
}
