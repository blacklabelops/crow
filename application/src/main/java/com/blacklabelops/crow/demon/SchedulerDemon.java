package com.blacklabelops.crow.demon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PreDestroy;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.config.Crow;
import com.blacklabelops.crow.config.Global;
import com.blacklabelops.crow.config.JobConfiguration;
import com.blacklabelops.crow.definition.ErrorMode;
import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.discover.LocalConfigDiscover;
import com.blacklabelops.crow.dispatcher.JobDispatcher;
import com.blacklabelops.crow.reporter.ExecutionErrorReporterFactory;
import com.blacklabelops.crow.reporter.IJobReporterFactory;
import com.blacklabelops.crow.repository.IJobRepositoryListener;
import com.blacklabelops.crow.repository.JobRepository;
import com.blacklabelops.crow.scheduler.CronUtilsExecutionTime;
import com.blacklabelops.crow.scheduler.IExecutionTime;
import com.blacklabelops.crow.scheduler.IScheduler;
import com.blacklabelops.crow.scheduler.Job;
import com.blacklabelops.crow.scheduler.JobScheduler;
import com.blacklabelops.crow.scheduler.MultiJobScheduler;

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

	private void createJob(JobConfiguration job) {
		LOG.info("Adding job '{}' to scheduler. Cron schedule '{}'", job.getName(), job.getCron());
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
		evaluateGlobalConfiguration().ifPresent(c -> this.jobRepository.setGlobalConfiguration(c));
		crowConfig.getJobs().stream().forEach(job -> createJob(job));
		localDiscoverer.discoverJobs().stream().forEach(job -> createJob(job));
		start();
	}

	private Optional<Global> evaluateGlobalConfiguration() {
		if (this.crowConfig != null && this.crowConfig.getGlobal() != null) {
			return Optional.of(this.crowConfig.getGlobal());
		}
		return localDiscoverer.discoverGlobalConfiguration();
	}

	@Override
	public void destroy() throws Exception {
	}

	public List<JobConfiguration> listJobs() {
		return crowConfig.getJobs();
	}

	@Override
	public void jobAdded(JobDefinition addedJobDefinition) {
		List<IJobReporterFactory> reporter = new ArrayList<>();
		if (!ErrorMode.CONTINUE.equals(addedJobDefinition.getErrorMode())) {
			reporter.add(new ExecutionErrorReporterFactory(jobScheduler));
		}
		IExecutionTime cronTime = new CronUtilsExecutionTime(addedJobDefinition.getCron());
		Job workJob = new Job(addedJobDefinition.resolveJobId(), cronTime);
		jobScheduler.addJob(workJob);
		jobDispatcher.addJob(addedJobDefinition, reporter, null);
	}

	@Override
	public void jobRemoved(JobDefinition removedJobDefinition) {

	}

}
