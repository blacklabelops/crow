package com.blacklabelops.crow.application;

import com.blacklabelops.crow.config.Crow;
import com.blacklabelops.crow.config.Environment;
import com.blacklabelops.crow.config.IConfigModel;
import com.blacklabelops.crow.config.Job;
import org.hibernate.validator.HibernateValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by steffenbleul on 29.12.16.
 */
public class ValidationTest {

    public static final String VALID_CRON = "* * * * *";
    public static Logger LOG = LoggerFactory.getLogger(ValidationTest.class);

    public LocalValidatorFactoryBean localValidatorFactory;

    public Job job;

    Set<ConstraintViolation<IConfigModel>> constraintViolations;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        job = new Job();
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
        job.setCommand("l");
        job.setCron(VALID_CRON);
        constraintViolations = localValidatorFactory.validate(job);
        assertEquals("Only 1 validation errors expected!",1,constraintViolations.size());
    }

    @Test
    public void whenJobNoCronThenInvalidateCorrectly() {
        job.setName("l");
        job.setCommand("l");
        constraintViolations = localValidatorFactory.validate(job);
        assertEquals("Only 1 validation errors expected!",1,constraintViolations.size());
    }

    @Test
    public void whenJobNoCommandThenInvalidateCorrectly() {
        job.setName("l");
        job.setCron(VALID_CRON);
        constraintViolations = localValidatorFactory.validate(job);
        assertEquals("Only 1 validation errors expected!",1,constraintViolations.size());
    }

    @Test
    public void whenJobCorrectlyConfiguredThenNoViolations() {
        job.setName("l");
        job.setCommand("l");
        job.setCron(VALID_CRON);
        constraintViolations = localValidatorFactory.validate(job);
        assertEquals("No validation errors expected!",0,constraintViolations.size());
    }

    @Test
    public void whenEnvironmentNoKeyThenInvalidateCorrectly() {
        Environment env = new Environment();
        constraintViolations = localValidatorFactory.validate(env);
        assertEquals("Only 1 validation errors expected!",1,constraintViolations.size());
    }

    @Test
    public void whenEnvironmentCorrectlyConfiguredThenNoViolations() {
        Environment env = new Environment();
        env.setKey("l");
        constraintViolations = localValidatorFactory.validate(env);
        assertEquals("No validation errors expected!",0,constraintViolations.size());
    }

    @Test
    public void whenJobEnvironmentsThenTheirKeysMustBeUnique() {
        job.setCron(VALID_CRON);
        job.setName("l");
        job.setCommand("l");
        Environment env = new Environment();
        env.setKey("l");
        job.getEnvironments().add(env);
        constraintViolations = localValidatorFactory.validate(job);
        assertEquals("No validation errors expected!",0,constraintViolations.size());
    }

    @Test
    public void whenTwoEnvironmentIdenticalKeysThenValidationViolation() {
        job.setCron(VALID_CRON);
        job.setName("l");
        job.setCommand("l");
        Environment env = new Environment();
        env.setKey("l");
        job.getEnvironments().add(env);
        Environment env2 = new Environment();
        env2.setKey("l");
        job.getEnvironments().add(env2);
        constraintViolations = localValidatorFactory.validate(job);
        assertEquals("Environment keys must be unique!",1,constraintViolations.size());
    }

    @Test
    public void whenJobWithoutValidCronThenViolation() {
        job.setCron("l");
        job.setName("l");
        job.setCommand("l");
        constraintViolations = localValidatorFactory.validate(job);
        assertEquals("Cron must be valid!",1,constraintViolations.size());
    }

    @Test
    public void whenJobWithValidCronThenNoViolation() {
        job.setCron(VALID_CRON);
        job.setName("l");
        job.setCommand("l");
        constraintViolations = localValidatorFactory.validate(job);
        assertEquals("Cron should be valid!",0,constraintViolations.size());
    }

    @Test
    public void whenCrowConfigJobNamesNotUnqiqueThenViolation() {
        Crow crow = new Crow();
        Job job1 = new Job();
        job1.setName("1");
        job1.setCron(VALID_CRON);
        job1.setCommand("1");
        crow.getJobs().add(job1);
        Job job2 = new Job();
        job2.setName("1");
        job2.setCron(VALID_CRON);
        job2.setCommand("1");
        crow.getJobs().add(job2);
        constraintViolations = localValidatorFactory.validate(crow);
        assertEquals("Job names must be unique!",1,constraintViolations.size());
    }

    @Test
    public void whenCrowConfigJobNamesUnqiqueThenNoViolation() {
        Crow crow = new Crow();
        Job job1 = new Job();
        job1.setName("1");
        job1.setCron(VALID_CRON);
        job1.setCommand("1");
        crow.getJobs().add(job1);
        Job job2 = new Job();
        job2.setName("2");
        job2.setCron(VALID_CRON);
        job2.setCommand("1");
        crow.getJobs().add(job2);
        constraintViolations = localValidatorFactory.validate(crow);
        assertEquals("Job names should be unique!",0,constraintViolations.size());
    }

    private void printViolations(Set<ConstraintViolation<IConfigModel>> constraintViolations) {
        if (constraintViolations != null && !constraintViolations.isEmpty()) {
            constraintViolations.stream().forEach(s -> LOG.debug(s.getMessage()));
        }
    }


}
