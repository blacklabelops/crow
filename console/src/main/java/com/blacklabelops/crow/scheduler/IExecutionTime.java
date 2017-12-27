package com.blacklabelops.crow.scheduler;

import java.time.ZonedDateTime;


public interface IExecutionTime {

    public ZonedDateTime nextExecution(ZonedDateTime date);
}
