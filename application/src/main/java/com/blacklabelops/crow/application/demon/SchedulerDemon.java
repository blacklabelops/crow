package com.blacklabelops.crow.application.demon;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.application.config.Crow;
import com.blacklabelops.crow.application.config.Global;
import com.blacklabelops.crow.application.config.JobConfiguration;
import com.blacklabelops.crow.application.discover.LocalConfigDiscover;
import com.blacklabelops.crow.application.dispatcher.JobDispatcher;
import com.blacklabelops.crow.application.repository.IJobRepositoryListener;
import com.blacklabelops.crow.application.repository.JobRepository;
import com.blacklabelops.crow.application.util.CrowConfiguration;
import com.blacklabelops.crow.application.util.GlobalCrowConfiguration;
import com.blacklabelops.crow.application.util.JobConfigurationConverter;
import com.blacklabelops.crow.application.util.JobConverter;
import com.blacklabelops.crow.console.definition.ErrorMode;
import com.blacklabelops.crow.console.definition.Job;
import com.blacklabelops.crow.console.reporter.ExecutionErrorReporterFactory;
import com.blacklabelops.crow.console.reporter.IJobReporterFactory;
import com.blacklabelops.crow.console.scheduler.CronUtilsExecutionTime;
import com.blacklabelops.crow.console.scheduler.IExecutionTime;
import com.blacklabelops.crow.console.scheduler.IScheduler;
import com.blacklabelops.crow.console.scheduler.JobScheduler;
import com.blacklabelops.crow.console.scheduler.MultiJobScheduler;
import com.blacklabelops.crow.console.scheduler.ScheduledJob;

@Component
public class SchedulerDemon implements CommandLineRunner, DisposableBean, IJobRepositoryListener {

	public static Logger LOG = LoggerFactory.getLogger(SchedulerDemon.class);

	private Crow crowConfig;

	private IScheduler jobScheduler;

	private JobDispatcher jobDispatcher;

	private MultiJobScheduler scheduler;

	private Thread schedulerThread;

	private JobRepository jobRepository;

	private LocalConfigDiscover localDiscoverer;

	@Autowired
	public SchedulerDemon(@Valid Crow config, JobRepository jobRepository, LocalConfigDiscover localDiscoverer,
			JobDispatcher jobDispatcher) {
		this.jobRepository = jobRepository;
		this.crowConfig = config;
		this.localDiscoverer = localDiscoverer;
		this.jobDispatcher = jobDispatcher;
		initialize();
	}

	private void initialize() {
		jobScheduler = new JobScheduler();
		scheduler = new MultiJobScheduler(jobScheduler, jobDispatcher.getDispatcher());
		this.jobRepository.addListener(this);
	}

	private void createJob(CrowConfiguration job) {
		LOG.info("Adding job '{}' to scheduler. Cron schedule '{}'", job.getJobName(), job.getCron());
		this.jobRepository.addJob(job);
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
			throw new RuntimeException("Thread could not be successfully joined!", e);
		}
	}

	@Override
	public void run(String... strings) throws Exception {
		createConfigurationJobs();
		localDiscoverer.discoverJobs().stream().forEach(job -> createJob(job));
		start();
	}

	private void createConfigurationJobs() {
		this.crowConfig.getJobs().stream().forEach(j -> createConfigurationJob(j, this.crowConfig.getGlobal()));
	}

	private void createConfigurationJob(JobConfiguration j, Global global) {
		JobConfigurationConverter crowConverter = new JobConfigurationConverter();
		GlobalCrowConfiguration crowGlobal = null;
		if (this.crowConfig.getGlobal() != null) {
			crowGlobal = crowConverter.convertGlobal(this.crowConfig.getGlobal());
		} else {
			crowGlobal = GlobalCrowConfiguration.builder().build();
		}
		CrowConfiguration crowConfig = crowConverter.convertJob(j);
		JobConverter converter = new JobConverter(crowGlobal);
		CrowConfiguration config = converter.convertJob(crowConfig);
		createJob(config);
	}

	@Override
	public void destroy() throws Exception {
	}

	public List<JobConfiguration> listJobs() {
		return crowConfig.getJobs();
	}

	@Override
	public void jobAdded(Job addedJobDefinition) {
		List<IJobReporterFactory> reporter = new ArrayList<>();
		if (!ErrorMode.CONTINUE.equals(addedJobDefinition.getErrorMode())) {
			reporter.add(new ExecutionErrorReporterFactory(jobScheduler));
		}
		IExecutionTime cronTime = new CronUtilsExecutionTime(addedJobDefinition.getCron().get());
		ScheduledJob workJob = new ScheduledJob(addedJobDefinition.getId(), cronTime);
		jobScheduler.addJob(workJob);
		jobDispatcher.addJob(addedJobDefinition, reporter, null);
	}

	@Override
	public void jobRemoved(Job removedJobDefinition) {

	}

}
