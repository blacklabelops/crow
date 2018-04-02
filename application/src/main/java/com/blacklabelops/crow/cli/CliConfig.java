package com.blacklabelops.crow.cli;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CliConfig {
	
	@Bean
	CommandLineConfiguration cliConfiguration() {
		return new CommandLineConfiguration();
	}
	
}
