package com.blacklabelops.crow.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.blacklabelops.crow.application.CrowDemon;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CrowDemon.class)
@TestPropertySource(locations="classpath:global.properties")
public class GlobalTest {
	
	@Autowired
    private Crow crow;
	
	@Test
	public void testGetGlobal_GlobalIsDefined_GlobalIsNotNull() {
		assertNotNull(crow.getGlobal());
	}
	
	@Test
	public void testGetGlobal_GlobalShellIsDefined_GlobalShellIsCorrect() {
		assertEquals("/bin/sh -c", crow.getGlobal().getShellCommand());
	}
	
	@Test
	public void testGetGlobal_GlobalWorkingDirectoryIsDefined_GlobalWorkingDirectoryIsCorrect() {
		assertEquals("/tmp", crow.getGlobal().getWorkingDirectory());
	}
	
	@Test
	public void testGetGlobal_ErrorModeIsDefined_ErrorModeIsCorrect() {
		assertEquals("stop", crow.getGlobal().getErrorMode());
	}
	
	@Test
	public void testGetGlobal_EnvironmentsDefined_EnvironmentsAsDefined() {
		assertEquals(1, crow.getGlobal().getEnvironments().size());
	}
	
}
