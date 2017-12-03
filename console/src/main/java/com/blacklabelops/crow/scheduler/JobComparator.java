package com.blacklabelops.crow.scheduler;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

/**
 * Created by steffenbleul on 22.12.16.
 */
public class JobComparator implements Comparator<Job> {

    private ChronoUnit chronoUnit = ChronoUnit.MILLIS;

    @Override
    public int compare(Job source, Job target) {
        ZonedDateTime nextExecutionSource = source.getNextExecution();
        ZonedDateTime nextExecutionTarget = target.getNextExecution();
        return Long.valueOf(chronoUnit.between(nextExecutionSource,nextExecutionTarget)).intValue();
    }
}
