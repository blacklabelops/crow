package com.blacklabelops.crow.application.repository;

import javax.validation.ConstraintViolationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.blacklabelops.crow.application.CrowDemon;
import com.blacklabelops.crow.application.model.CrowConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CrowDemon.class)
public class JobRepositoryJobValidationIT {

	@Autowired
	public JobRepository repository;

	@Test(expected = ConstraintViolationException.class)
	public void testJobValidation_InvalidCron_ValidationError() {
		CrowConfiguration config = CrowConfiguration.builder()
				.jobName("jobName")
				.cron("ewefwe")
				.build();
		repository.addJob(config);
	}
}
