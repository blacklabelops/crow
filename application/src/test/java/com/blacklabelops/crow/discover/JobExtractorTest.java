package com.blacklabelops.crow.discover;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.blacklabelops.crow.config.JobConfiguration;

public class JobExtractorTest {
	
	public Map<String, String> values;
	
	@Before
	public void setup() {
		values = new HashMap<>();
	}
	
	@Test
	public void testExtraction_WhenNoValuesDefined_EmptyJobs() {
		JobExtractor extractor = new JobExtractor("JOB");
		List<JobConfiguration> jobs = extractor.extractFromEnvironmentVariables(values);
		assertTrue(jobs.isEmpty());
	}
	
	@Test
	public void testExtraction_WhenJobNameDEfined_JobIsFound() {
		JobExtractor extractor = new JobExtractor("JOB");
		values.put("JOB1NAME", "name");
		List<JobConfiguration> jobs = extractor.extractFromEnvironmentVariables(values);
		assertEquals(1, jobs.size());
		assertEquals("name", jobs.get(0).getName());
	}
	
	@Test
	public void testExtraction_WhenJobNameInPropertiesDefined_JobIsFound() {
		JobExtractor extractor = new JobExtractor("job.");
		values.put("job.1.name", "name");
		List<JobConfiguration> jobs = extractor.extractFromProperties(values);
		assertEquals(1, jobs.size());
		assertEquals("name", jobs.get(0).getName());
	}
	
	@Test
	public void testExtraction_WhenTimeoutInvalidInPropertiesDefined_JobIsNotAdded() {
		JobExtractor extractor = new JobExtractor("job.");
		values.put("job.1.name", "name");
		values.put("job.1.timeout.minutes", "invalid");
		List<JobConfiguration> jobs = extractor.extractFromProperties(values);
		assertTrue(jobs.isEmpty());
	}
}
