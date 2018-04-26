package com.blacklabelops.crow.application.discover.docker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfiguration implements SchedulingConfigurer {

	public static Logger LOG = LoggerFactory.getLogger(SchedulerConfiguration.class);

	@Autowired
	private DockerCrawler crawler;

	@Autowired
	private DockerCrawlerConfiguration configuration;

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		if (!configuration.resolveCrawlerEnabled()) {
			return;
		}
		LOG.info("Docker crawler enabled!");
		taskRegistrar.addTriggerTask(
				new Runnable() {
					@Override
					public void run() {
						crawler.discoverJobs();
					}
				},
				new Trigger() {
					@Override
					public Date nextExecutionTime(TriggerContext triggerContext) {
						Calendar nextExecutionTime = new GregorianCalendar();
						Date lastActualExecutionTime = triggerContext.lastActualExecutionTime();
						nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime
								: new Date());
						nextExecutionTime.add(Calendar.SECOND, 20);
						return nextExecutionTime.getTime();
					}
				});
	}

}
