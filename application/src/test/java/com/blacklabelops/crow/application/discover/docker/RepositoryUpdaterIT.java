package com.blacklabelops.crow.application.discover.docker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.blacklabelops.crow.application.model.CrowConfiguration;
import com.blacklabelops.crow.application.repository.IJobRepositoryListener;
import com.blacklabelops.crow.application.repository.JobRepository;
import com.blacklabelops.crow.console.definition.Job;

public class RepositoryUpdaterIT implements IJobRepositoryListener {

	RepositoryUpdater updater;

	JobRepository repository;

	List<CrowConfiguration> configs;

	List<Job> addedJobs;

	List<Job> removedJobs;

	Map<Job, Job> updatedJobs;

	@Before
	public void setupClass() {
		repository = new JobRepository();
		repository.addListener(this);
		updater = new RepositoryUpdater(repository);
		addedJobs = new ArrayList<>();
		removedJobs = new ArrayList<>();
		updatedJobs = new HashMap<>();
		configs = new ArrayList<>();
	}

	@Override
	public void jobAdded(Job addedJobDefinition) {
		addedJobs.add(addedJobDefinition);
	}

	@Override
	public void jobRemoved(Job removedJobDefinition) {
		removedJobs.add(removedJobDefinition);
	}

	@Override
	public void jobUpdated(Job oldJob, Job newJob) {
		updatedJobs.put(oldJob, newJob);
	}

	@Test
	public void testAddJob_JobAdded_JobAddedInRepository() {
		CrowConfiguration config = CrowConfiguration.builder()
				.jobName("job")
				.command("command")
				.build();
		configs.add(config);

		updater.notifyRepository(configs);

		assertTrue(addedJobs.size() == 1);
		assertEquals(config.getJobName().get(), addedJobs.stream().findFirst().get().getName());
	}

	@Test
	public void testAddJob_JobDoubleAdded_JobAddedInRepositoryOnce() {
		CrowConfiguration config = CrowConfiguration.builder()
				.jobName("job")
				.command("command")
				.build();
		configs.add(config);

		updater.notifyRepository(configs);
		updater.notifyRepository(configs);

		assertTrue(addedJobs.size() == 1);
		assertEquals(config.getJobName().get(), addedJobs.stream().findFirst().get().getName());
	}

	@Test
	public void testAddJob_JobNotExistentAtSecondNotify_JobRemoved() {
		CrowConfiguration config = CrowConfiguration.builder()
				.jobName("job")
				.command("command")
				.build();
		configs.add(config);

		updater.notifyRepository(configs);
		updater.notifyRepository(new ArrayList<>());

		assertTrue(removedJobs.size() == 1);
		assertTrue(repository.listJobs().isEmpty());
		assertEquals(config.getJobName().get(), removedJobs.stream().findFirst().get().getName());
	}

	@Test
	public void testAddJob_JobChangedAtSecondNotify_JobUpdated() {
		CrowConfiguration config = CrowConfiguration.builder()
				.jobName("job")
				.command("command")
				.build();
		configs.add(config);

		updater.notifyRepository(configs);
		configs.clear();
		configs.add(config.withCommand("newCommand"));
		updater.notifyRepository(configs);

		assertTrue(updatedJobs.size() == 1);
		assertTrue(repository.listJobs().size() == 1);
		assertEquals(config.getJobName().get(), updatedJobs.values().stream().findFirst().get().getName());
	}

}
