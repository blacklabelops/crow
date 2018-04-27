package com.blacklabelops.crow.application.discover;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.blacklabelops.crow.application.model.CrowConfiguration;
import com.blacklabelops.crow.application.model.GlobalCrowConfiguration;

public class JobConverterTest {

	public JobConverter converter;

	@Test
	public void testConversion_WhenShellCommandDefined_CommandWithoutShellCommand() {
		GlobalCrowConfiguration global = GlobalCrowConfiguration.builder()
				.shellCommand("sh -c").build();
		converter = new JobConverter(global);
		CrowConfiguration config = CrowConfiguration.builder()
				.jobName("name")
				.command("echo \"Hello World!\"")
				.cron("* * * * *").build();

		CrowConfiguration result = converter.convertJob(config);

		assertNotNull(result);
		assertEquals("echo \"Hello World!\"", result.getCommand().get());
		assertEquals("sh -c", result.getShellCommand().get());
	}
}
