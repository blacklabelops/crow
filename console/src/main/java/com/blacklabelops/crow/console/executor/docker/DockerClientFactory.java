package com.blacklabelops.crow.console.executor.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.console.executor.ExecutorException;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DefaultDockerClient.Builder;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;

public class DockerClientFactory {

	public static final Logger LOG = LoggerFactory.getLogger(DockerClientFactory.class);

	public static DockerClient initializeDockerClient() {
		DockerClient dockerClient = null;
		try {
			Builder builder = DefaultDockerClient.fromEnv();
			if (!builder.uri().getScheme().equals("https") && builder.dockerCertificates() != null) {
				builder.dockerCertificates(null);
			}
			dockerClient = builder.build();
		} catch (DockerCertificateException e) {
			String message = String.format("Unable to initialize Docker Client!");
			LOG.error(message, e);
			throw new ExecutorException(message, e);
		}
		return dockerClient;
	}
}
