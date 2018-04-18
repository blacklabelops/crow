package com.blacklabelops.crow.application.cli;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CliConfig {
	
	@Bean
	CommandLineConfiguration cliConfiguration() {
		return new CommandLineConfiguration();
	}
	
}
