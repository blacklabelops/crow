package com.blacklabelops.crow.console.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class JobImmutableTest {

	@Test
	public void testImmutable_ImmutableBuilder_ImmutableCreated() {
		Job job = Job.builder().id(JobId.builder().id("id").build()).name("name").build();

		assertNotNull(job);
		assertEquals("id", job.getId().getId());
		assertEquals("name", job.getName());
	}

	@Test
	public void testImmutable_ImmutableUsingConstructors_ImmutableCreated() {
		Job job = Job.of(JobId.of("id"), "name");
		assertNotNull(job);
		assertEquals("id", job.getId().getId());
		assertEquals("name", job.getName());
	}

	@Test
	public void testImmutable_ImmutableUsingWrapperInitializer_ImmutableCreated() {
		Job job = Job.builder().id("id").name("name").build();
		assertNotNull(job);
		assertEquals("id", job.getId().getId());
		assertEquals("name", job.getName());
	}

	@Test
	public void testImmutable_NewImmutableUsingWither_ImmutableChanged() {
		Job job = Job.builder().id("id").name("name").build();

		assertNotNull(job);
		assertEquals("id", job.getId().getId());
		assertEquals("name", job.getName());
	}

}
