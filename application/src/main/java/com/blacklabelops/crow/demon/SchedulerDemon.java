package com.blacklabelops.crow.demon;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;
import javax.validation.Valid;

import org.apache.tools.ant.types.Commandline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.config.Crow;
import com.blacklabelops.crow.config.Job;
import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.executor.ErrorMode;
import com.blacklabelops.crow.executor.ExecutionMode;
import com.blacklabelops.crow.executor.IExecutorTemplate;
import com.blacklabelops.crow.executor.JobExecutorTemplate;
import com.blacklabelops.crow.logger.JobLoggerFactory;
import com.blacklabelops.crow.reporter.ConsoleReporterFactory;
import com.blacklabelops.crow.reporter.ExecutionErrorReporterFactory;
import com.blacklabelops.crow.reporter.IJobReporterFactory;
import com.blacklabelops.crow.scheduler.CronUtilsExecutionTime;
import com.blacklabelops.crow.scheduler.IExecutionTime;
import com.blacklabelops.crow.scheduler.IScheduler;
import com.blacklabelops.crow.scheduler.JobScheduler;
import com.blacklabelops.crow.scheduler.MultiJobScheduler;


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
        JobDefinition defConsole = new JobDefinition();
        List<IJobReporterFactory> reporter = new ArrayList<>();
        reporter.add(new ConsoleReporterFactory());
        defConsole.setCommand(takeOverCommand(job.getCommand(), job.getShellCommand()));
        defConsole.setPreCommand(takeOverCommand(job.getPreCommand(), job.getShellCommand()));
        defConsole.setPostCommand(takeOverCommand(job.getPostCommand(), job.getShellCommand()));
        if (job.getWorkingDirectory() != null && !job.getWorkingDirectory().isEmpty()) {
            File workingDirectory = new File(job.getWorkingDirectory());
            if (workingDirectory.exists() && workingDirectory.isDirectory()) {
                defConsole.setWorkingDir(workingDirectory);
            }
        }
        if (!job.getEnvironments().isEmpty()) {
            defConsole.setEnvironmentVariables(createEnvironmentVariables(job.getEnvironments()));
        }
        defConsole.setJobName(job.getName());
        defConsole.setTimeoutMinutes(job.getTimeOutMinutes());
        defConsole.setExecutionMode(ExecutionMode.getMode(job.getExecution()));
        takeOverErrorMode(job, defConsole, reporter);
        IExecutorTemplate jobTemplate = new JobExecutorTemplate(defConsole,reporter, Stream.of(new JobLoggerFactory(job.getName())).collect(Collectors.toList()));
        IExecutionTime cronTime = new CronUtilsExecutionTime(job.getCron());
        com.blacklabelops.crow.scheduler.Job workJob = new com.blacklabelops.crow.scheduler.Job(jobTemplate, cronTime);
        jobScheduler.addJob(workJob);
    }

    private String[] takeOverCommand(String command, String shellCommand) {
        Commandline commandLine = null;
        if (shellCommand != null && !shellCommand.isEmpty()) {
            commandLine = new Commandline( shellCommand );
            commandLine.addArguments(new String[] { command });
        } else {
            commandLine = new Commandline(command);
        }
        return commandLine.getCommandline();
    }

    private void takeOverErrorMode(Job job, JobDefinition defConsole, List<IJobReporterFactory> reporter) {
        defConsole.setErrorMode(ErrorMode.getMode(job.getErrorMode()));
        if (!ErrorMode.CONTINUE.equals(defConsole.getErrorMode())) {
            reporter.add(new ExecutionErrorReporterFactory(jobScheduler));
        }
    }

    private Map<String,String> createEnvironmentVariables(Map<String, String> environments) {
        Map<String, String> environmentVariables = new HashMap<>();
        environments.keySet().stream().forEach(key -> environmentVariables.put(key,environments.get(key) != null ? environments.get(key) : ""));
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
