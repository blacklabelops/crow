package com.blacklabelops.crow.application.application;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.validator.HibernateValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.blacklabelops.crow.application.config.IConfigModel;
import com.blacklabelops.crow.application.model.CrowConfiguration;

public class ValidationTest {

	public static final String VALID_CRON = "* * * * *";
	public static Logger LOG = LoggerFactory.getLogger(ValidationTest.class);

	public LocalValidatorFactoryBean localValidatorFactory;

	Set<ConstraintViolation<IConfigModel>> constraintViolations;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setup() {
		localValidatorFactory = new LocalValidatorFactoryBean();
		localValidatorFactory.setProviderClass(HibernateValidator.class);
		localValidatorFactory.afterPropertiesSet();
	}

	@After
	public void tearDown() {
		printViolations(constraintViolations);
		constraintViolations = null;
	}

	@Test
	public void whenJobNoNameThenInvalidateNameCorrectly() {
		CrowConfiguration job = CrowConfiguration.builder()
				.command("l")
				.cron(VALID_CRON)
				.build();
		constraintViolations = localValidatorFactory.validate(job);
		assertEquals("Only 1 validation errors expected!", 1, constraintViolations.size());
		assertEquals("jobName", constraintViolations.iterator().next().getPropertyPath().toString());
	}

	@Test
	public void whenJobNoCronThenInvalidateCorrectly() {
		CrowConfiguration job = CrowConfiguration.builder()
				.jobName("l")
				.command("l")
				.build();
		constraintViolations = localValidatorFactory.validate(job);
		assertEquals("Only 1 validation errors expected!", 1, constraintViolations.size());
		assertEquals("cron", constraintViolations.iterator().next().getPropertyPath().toString());
	}

	@Test
	public void whenJobNoCommandThenInvalidateCorrectly() {
		CrowConfiguration job = CrowConfiguration.builder()
				.jobName("l")
				.cron(VALID_CRON)
				.build();
		constraintViolations = localValidatorFactory.validate(job);
		assertEquals("Only 1 validation errors expected!", 1, constraintViolations.size());
		assertEquals("command", constraintViolations.iterator().next().getPropertyPath().toString());
	}

	@Test
	public void whenJobCorrectlyConfiguredThenNoViolations() {
		CrowConfiguration job = CrowConfiguration.builder()
				.jobName("l")
				.command("l")
				.cron(VALID_CRON)
				.build();
		constraintViolations = localValidatorFactory.validate(job);
		assertEquals("No validation errors expected!", 0, constraintViolations.size());
	}

	@Test
	public void whenJobWithoutValidCronThenViolation() {
		CrowConfiguration job = CrowConfiguration.builder()
				.jobName("l")
				.command("l")
				.build();
		constraintViolations = localValidatorFactory.validate(job);
		assertEquals("Cron must be valid!", 1, constraintViolations.size());
		assertEquals("cron", constraintViolations.iterator().next().getPropertyPath().toString());
	}

	@Test
	public void whenJobWithValidCronThenNoViolation() {
		CrowConfiguration job = CrowConfiguration.builder()
				.jobName("l")
				.command("l")
				.cron(VALID_CRON)
				.build();
		constraintViolations = localValidatorFactory.validate(job);
		assertEquals("Cron should be valid!", 0, constraintViolations.size());
	}

	@Test
	public void whenNameWithSpaces_RegExViolation() {
		CrowConfiguration job = CrowConfiguration.builder()
				.jobName("aaa bbb")
				.command("command")
				.cron(VALID_CRON)
				.build();
		constraintViolations = localValidatorFactory.validate(job);
		assertEquals("Job name not allowed!", 1, constraintViolations.size());
		assertEquals("jobName", constraintViolations.iterator().next().getPropertyPath().toString());
	}

	@Test
	public void whenNegativeTimeOut_Violation() {
		CrowConfiguration job = CrowConfiguration.builder()
				.jobName("aaa.bbb")
				.command("command")
				.cron(VALID_CRON)
				.timeOutMinutes(-500)
				.build();
		constraintViolations = localValidatorFactory.validate(job);
		assertEquals(1, constraintViolations.size());
		assertEquals("timeOutMinutes", constraintViolations.iterator().next().getPropertyPath().toString());
	}

	private void printViolations(Set<ConstraintViolation<IConfigModel>> constraintViolations2) {
		if (constraintViolations2 != null && !constraintViolations2.isEmpty()) {
			constraintViolations2.stream().forEach(s -> LOG.debug(s.getMessage()));
		}
	}

}
