package com.blacklabelops.crow.application.discover;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.blacklabelops.crow.application.model.GlobalCrowConfiguration;

public class GlobalExtractorTest {

	public Map<String, String> values;

	@Before
	public void setup() {
		values = new HashMap<>();
	}

	@Test
	public void testExtraction_WhenNoValuesDefined_NullConfig() {
		GlobalExtractor extractor = new GlobalExtractor("CROW_");
		Optional<GlobalCrowConfiguration> global = extractor.extractGlobalFromEnvironmentVariables(values);
		assertFalse(global.isPresent());
	}

	@Test
	public void testExtraction_WhenShellDefined_ShellFound() {
		GlobalExtractor extractor = new GlobalExtractor("CROW_");
		this.values.put("CROW_SHELL_COMMAND", "shellcommand");
		Optional<GlobalCrowConfiguration> global = extractor.extractGlobalFromEnvironmentVariables(values);
		assertEquals("shellcommand", global.get().getShellCommand().get());
	}

	@Test
	public void testExtraction_WhenShellPropertiesDefined_ShellFound() {
		GlobalExtractor extractor = new GlobalExtractor("crow.");
		this.values.put("crow.shell.command", "shellcommand");
		Optional<GlobalCrowConfiguration> global = extractor.extractGlobalFromProperties(values);
		assertEquals("shellcommand", global.get().getShellCommand().get());
	}
}
