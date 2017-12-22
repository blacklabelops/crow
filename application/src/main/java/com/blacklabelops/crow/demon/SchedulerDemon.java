package com.blacklabelops.crow.demon;

import com.blacklabelops.crow.config.Crow;
import com.blacklabelops.crow.config.Job;
import com.blacklabelops.crow.executor.ErrorMode;
import com.blacklabelops.crow.executor.ExecutionMode;
import com.blacklabelops.crow.executor.IExecutor;
import com.blacklabelops.crow.executor.SimpleConsole;
import com.blacklabelops.crow.executor.console.JobDefinition;
import com.blacklabelops.crow.logger.JobLogLogger;
import com.blacklabelops.crow.reporter.ConsoleReporter;
import com.blacklabelops.crow.reporter.ExecutionErrorReporter;
import com.blacklabelops.crow.reporter.IJobReporter;
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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        JobDefinition defConsole = new JobDefinition();
        List<IJobReporter> reporter = new ArrayList<>();
        reporter.add(new ConsoleReporter());
        takeOverCommand(job, defConsole);
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
        defConsole.setExecutionMode(ExecutionMode.getMode(job.getExecution()));
        takeOverErrorMode(job, defConsole, reporter);
        IExecutor simepleConsole = new SimpleConsole(defConsole,reporter, Stream.of(new JobLogLogger(job.getName())).collect(Collectors.toList()));
        IExecutionTime cronTime = new CronUtilsExecutionTime(job.getCron());
        com.blacklabelops.crow.scheduler.Job workJob = new com.blacklabelops.crow.scheduler.Job(simepleConsole, cronTime);
        jobScheduler.addJob(workJob);
    }

    private void takeOverCommand(Job job, JobDefinition defConsole) {
        Commandline commandLine = null;
        defConsole.setCommand(Commandline.translateCommandline(job.getCommand()));
        if (job.getShellCommand() != null && !job.getShellCommand().isEmpty()) {
            commandLine = new Commandline(job.getShellCommand());
            commandLine.addArguments(new String[] {job.getCommand()});
        } else {
            commandLine = new Commandline(job.getCommand());
        }
        defConsole.setCommand(commandLine.getCommandline());
    }

    private void takeOverErrorMode(Job job, JobDefinition defConsole, List<IJobReporter> reporter) {
        defConsole.setErrorMode(ErrorMode.getMode(job.getErrorMode()));
        if (!ErrorMode.CONTINUE.equals(defConsole.getErrorMode())) {
            reporter.add(new ExecutionErrorReporter(jobScheduler));
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
