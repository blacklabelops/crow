package com.blacklabelops.crow.cli;

import org.springframework.beans.factory.annotation.Value;

public class CommandLineConfiguration {
	
	@Value("${crow.server.baseUrl:http://localhost:8080}")
	private String baseUrl;
	
	public CommandLineConfiguration() {
		super();
	}
	
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
}
