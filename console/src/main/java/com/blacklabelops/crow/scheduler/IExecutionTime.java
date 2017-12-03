package com.blacklabelops.crow.scheduler;

import java.time.ZonedDateTime;

/**
 * Created by steffenbleul on 22.12.16.
 */
public interface IExecutionTime {

    public ZonedDateTime nextExecution(ZonedDateTime date);
}
