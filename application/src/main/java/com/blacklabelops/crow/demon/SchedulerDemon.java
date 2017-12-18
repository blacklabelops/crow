package com.blacklabelops.crow.demon;

import com.blacklabelops.crow.config.Crow;
import com.blacklabelops.crow.config.Environment;
import com.blacklabelops.crow.config.Job;
import com.blacklabelops.crow.executor.IExecutor;
import com.blacklabelops.crow.executor.SimpleConsole;
import com.blacklabelops.crow.executor.console.DefinitionConsole;
import com.blacklabelops.crow.logger.JobLogLogger;
import com.blacklabelops.crow.scheduler.*;
import org.apache.tools.ant.types.Commandline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by steffenbleul on 28.12.16.
 */
@Component
public class SchedulerDemon implements CommandLineRunner, DisposableBean {

    public static Logger LOG = LoggerFactory.getLogger(SchedulerDemon.class);

    private Crow crowConfig;

    private IScheduler jobScheduler;

    private MultiJobScheduler scheduler;

    private Thread schedulerThread;

    @Autowired
    public SchedulerDemon(@Valid Crow config) {
        this.crowConfig = config;
        initialize();
    }

    private void initialize() {
        jobScheduler = new JobScheduler();
        scheduler = new MultiJobScheduler(jobScheduler);
        crowConfig.getJobs().stream().forEach(job -> createJob(job));
    }

    private void createJob(Job job) {
        LOG.info("Adding job '{}' to scheduler. Cron schedule '{}'", job.getName(), job.getCron());
        DefinitionConsole defConsole = new DefinitionConsole();
        defConsole.setCommand(Commandline.translateCommandline(job.getCommand()));
        if (!job.getEnvironments().isEmpty()) {
            defConsole.setEnvironmentVariables(createEnvironmentVariables(job.getEnvironments()));
        }
        defConsole.setJobName(job.getName());
        IExecutor simepleConsole = new SimpleConsole(defConsole,null, new JobLogLogger(job.getName()));
        IExecutionTime cronTime = new CronUtilsExecutionTime(job.getCron());
        com.blacklabelops.crow.scheduler.Job workJob = new com.blacklabelops.crow.scheduler.Job(simepleConsole, cronTime);
        jobScheduler.addJob(workJob);
    }

    private Map<String,String> createEnvironmentVariables(List<Environment> environments) {
        Map<String, String> environmentVariables = new HashMap<>();
        environments.stream().forEach(environment -> environmentVariables.put(environment.getKey(),environment.getValue() != null ? environment.getValue() : ""));
        return environmentVariables;
    }

    public void start() {
        LOG.debug("Starting Scheduler");
        schedulerThread = new Thread(scheduler);
        schedulerThread.start();
    }

    @PreDestroy
    public void stop() {
        LOG.debug("Stopping Scheduler");
        scheduler.stop();
        try {
            schedulerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread could not be successfully joined!",e);
        }
    }

    @Override
    public void run(String... strings) throws Exception {
        start();
    }

    @Override
    public void destroy() throws Exception {
    }
}
