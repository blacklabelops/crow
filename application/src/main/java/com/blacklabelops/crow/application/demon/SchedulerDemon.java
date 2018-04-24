package com.blacklabelops.crow.application.demon;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.blacklabelops.crow.application.discover.enironment.LocalConfigDiscover;
import com.blacklabelops.crow.application.discover.file.ConfigFileDiscover;
import com.blacklabelops.crow.application.dispatcher.JobDispatcher;
import com.blacklabelops.crow.application.model.CrowConfiguration;
import com.blacklabelops.crow.application.repository.IJobRepositoryListener;
import com.blacklabelops.crow.application.repository.JobRepository;
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

	private IScheduler jobScheduler;

	private JobDispatcher jobDispatcher;

	private ConfigFileDiscover configurationDiscoverer;

	private MultiJobScheduler scheduler;

	private Thread schedulerThread;

	private JobRepository jobRepository;

	private LocalConfigDiscover localDiscoverer;

	@Autowired
	public SchedulerDemon(JobRepository jobRepository, LocalConfigDiscover localDiscoverer,
			JobDispatcher jobDispatcher, ConfigFileDiscover configurationDiscoverer) {
		this.configurationDiscoverer = configurationDiscoverer;
		this.jobRepository = jobRepository;
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
		this.configurationDiscoverer.discoverJobs().stream().forEach(j -> this.createJob(j));
		localDiscoverer.discoverJobs().stream().forEach(job -> createJob(job));
		start();
	}

	@Override
	public void destroy() throws Exception {
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

	@Override
	public void jobUpdated(Job oldJob, Job newJob) {
		// TODO Auto-generated method stub

	}

}
