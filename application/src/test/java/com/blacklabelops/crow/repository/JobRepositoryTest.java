package com.blacklabelops.crow.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.blacklabelops.crow.config.JobConfiguration;
import com.blacklabelops.crow.definition.JobDefinition;

public class JobRepositoryTest {
	
	@Rule public MockitoRule mockito = MockitoJUnit.rule();
	
	@Mock public IJobRepositoryListener listener;
	
	@Test
	public void testAddJob_WhenAddingTwoJobs_SecondWillBeDismissed() {
		JobConfiguration config = new JobConfiguration();
		config.setName("a");
		JobRepository rep = new JobRepository();
		rep.addListener(listener);
		
		rep.addJob(config);
		rep.addJob(config);
		
		verify(listener, times(1)).jobAdded(any(JobDefinition.class));
	}
	
	@Test
	public void testAddJob_WhenAddingTwoDifferentJobs_BothWillBeAdded() {
		JobConfiguration config = new JobConfiguration();
		config.setName("a");
		JobRepository rep = new JobRepository();
		rep.addListener(listener);
		
		rep.addJob(config);
		JobConfiguration configB = new JobConfiguration();
		configB.setName("b");
		rep.addJob(configB);
		
		verify(listener, times(2)).jobAdded(any(JobDefinition.class));
	}
	
	@Test
	public void testRemoveJob_WhenRemovingJob_JobWillBeRemoved() {
		JobConfiguration config = new JobConfiguration();
		config.setName("a");
		JobRepository rep = new JobRepository();
		rep.addListener(listener);
		rep.addJob(config);
		
		rep.removeJob("a");
		
		verify(listener, times(1)).jobAdded(any(JobDefinition.class));
		verify(listener, times(1)).jobRemoved(any(JobDefinition.class));
	}
	
	@Test
	public void testRemoveJob_WhenRemovingUnknownJob_NoJobWillBeRemoved() {
		JobConfiguration config = new JobConfiguration();
		config.setName("a");
		JobRepository rep = new JobRepository();
		rep.addListener(listener);
		rep.addJob(config);
		
		rep.removeJob("b");
		
		verify(listener, times(1)).jobAdded(any(JobDefinition.class));
		verify(listener, never()).jobRemoved(nullable(JobDefinition.class));
	}
	
}