package com.blacklabelops.crow.application.discover;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.blacklabelops.crow.application.util.CrowConfiguration;

public class JobExtractorTest {

	public Map<String, String> values;

	@Before
	public void setup() {
		values = new HashMap<>();
	}

	@Test
	public void testExtraction_WhenNoValuesDefined_EmptyJobs() {
		JobExtractor extractor = new JobExtractor("JOB");
		List<CrowConfiguration> jobs = extractor.extractFromEnvironmentVariables(values);
		assertTrue(jobs.isEmpty());
	}

	@Test
	public void testExtraction_WhenJobNameDEfined_JobIsFound() {
		JobExtractor extractor = new JobExtractor("JOB");
		values.put("JOB1NAME", "name");
		List<CrowConfiguration> jobs = extractor.extractFromEnvironmentVariables(values);
		assertEquals(1, jobs.size());
		assertEquals("name", jobs.get(0).getJobName().orElse(null));
	}

	@Test
	public void testExtraction_WhenJobNameInPropertiesDefined_JobIsFound() {
		JobExtractor extractor = new JobExtractor("job.");
		values.put("job.1.name", "name");
		List<CrowConfiguration> jobs = extractor.extractFromProperties(values);
		assertEquals(1, jobs.size());
		assertEquals("name", jobs.get(0).getJobName().orElse(null));
	}

	@Test
	public void testExtraction_WhenContainerNameInPropertiesDefined_JobIsFound() {
		JobExtractor extractor = new JobExtractor("job.");
		values.put("job.1.container.name", "containerName");
		List<CrowConfiguration> jobs = extractor.extractFromProperties(values);
		assertEquals(1, jobs.size());
		assertEquals("containerName", jobs.get(0).getContainerName().orElse(null));
	}

	@Test
	public void testExtraction_WhenContainerIdInPropertiesDefined_JobIsFound() {
		JobExtractor extractor = new JobExtractor("job.");
		values.put("job.1.container.id", "containerId");
		List<CrowConfiguration> jobs = extractor.extractFromProperties(values);
		assertEquals(1, jobs.size());
		assertEquals("containerId", jobs.get(0).getContainerId().orElse(null));
	}

	@Test
	public void testExtraction_WhenTimeoutInvalidInPropertiesDefined_JobIsNotAdded() {
		JobExtractor extractor = new JobExtractor("job.");
		values.put("job.1.name", "name");
		values.put("job.1.timeout.minutes", "invalid");
		List<CrowConfiguration> jobs = extractor.extractFromProperties(values);
		assertTrue(jobs.isEmpty());
	}
}
