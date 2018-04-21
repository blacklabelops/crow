package com.blacklabelops.crow.application.discover.docker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
class DockerCrawlerConfiguration {

	@Value("${crow.docker.crawler.enabled:false}")
	private String dockerCrawlerEnabled;

	@Value("${CROW_DOCKER_CRAWLER_ENABLED:false}")
	private String dockerCrawlerEnabledEnv;

	public DockerCrawlerConfiguration() {
		super();
	}

	public String getDockerCrawlerEnabled() {
		return dockerCrawlerEnabled;
	}

	public String getDockerCrawlerEnabledEnv() {
		return dockerCrawlerEnabledEnv;
	}

	public boolean resolveCrawlerEnabled() {
		boolean crawlerEnabled = false;
		if (Boolean.TRUE.equals(Boolean.valueOf(getDockerCrawlerEnabledEnv()))) {
			crawlerEnabled = true;
		} else if (Boolean.TRUE.equals(Boolean.valueOf(dockerCrawlerEnabled))) {
			crawlerEnabled = true;
		}
		return crawlerEnabled;
	}

}
