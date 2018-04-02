package com.blacklabelops.crow.cli;

import org.springframework.beans.factory.annotation.Value;

import com.cronutils.utils.StringUtils;

public class CommandLineConfiguration {
	
	@Value("${crow.server.baseUrl:http://localhost:8080}")
	private String baseUrl;
	
	@Value("${CROW_BASE_URL:}")
	private String envBaseUrl;
	
	public CommandLineConfiguration() {
		super();
	}
	
	public String evaluateBaseURL() {
		String evaluatedBaseURL = this.envBaseUrl;
		if (StringUtils.isEmpty(evaluatedBaseURL)) {
			evaluatedBaseURL = baseUrl;
		}
		return evaluatedBaseURL;
	}

	
	
}
