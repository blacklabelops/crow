package com.blacklabelops.crow.application.application;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.blacklabelops.crow.application.CrowDemon;
import com.blacklabelops.crow.application.demon.SchedulerDemon;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CrowDemon.class)
@TestPropertySource(locations = "classpath:test.properties")
public class SchedulerDemonTry {

	@Autowired
	public SchedulerDemon demon;

	@Test
	public void whenSuccessfullyInitializedThenNotNull() {
		assertNotNull(demon);
	}

}
