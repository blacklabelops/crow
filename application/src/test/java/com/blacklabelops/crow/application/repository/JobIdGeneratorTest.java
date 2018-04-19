package com.blacklabelops.crow.application.repository;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class JobIdGeneratorTest {

	public JobIdGenerator generator = new JobIdGenerator();

	@Test
	public void testId_GenerateId_NotNull() {
		assertNotNull(generator.generate());
	}
}
