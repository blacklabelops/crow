package com.blacklabelops.crow.console.scheduler;

import java.time.ZonedDateTime;


public interface IExecutionTime {

    public ZonedDateTime nextExecution(ZonedDateTime date);
}
