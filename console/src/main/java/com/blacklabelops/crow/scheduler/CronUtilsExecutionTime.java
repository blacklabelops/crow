package com.blacklabelops.crow.scheduler;

import java.time.ZonedDateTime;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;


public class CronUtilsExecutionTime implements IExecutionTime {

    private final String cronString;

    private ExecutionTime executionTime;

    public CronUtilsExecutionTime(String cronTime) {
        super();
        cronString = cronTime;
        executionTime = initializeExecutionTime();
    }

    private ExecutionTime initializeExecutionTime() {
        ExecutionTime executionTime = null;
        CronDefinition cronD =
                CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronD);
        Cron cron = parser.parse(cronString);
        executionTime = ExecutionTime.forCron(cron);
        return executionTime;
    }

    public static void validate() {
        CronDefinition cronD =
                CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        new CronParser(cronD);
    }

    @Override
    public ZonedDateTime nextExecution(ZonedDateTime date) {
        return executionTime.nextExecution(date);
    }
}
