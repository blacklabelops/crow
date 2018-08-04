package com.blacklabelops.crow.docker.client.spotify.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.docker.client.DockerClientException;
import com.blacklabelops.crow.docker.client.test.IDockerClientTest;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DefaultDockerClient.Builder;
import com.spotify.docker.client.exceptions.DockerCertificateException;

public class DockerTestClientFactory {
	public static final Logger LOG = LoggerFactory.getLogger(DockerTestClientFactory.class);

	public static IDockerClientTest initializeDockerClient() {
		IDockerClientTest dockerClient = null;
		try {
			Builder builder = DefaultDockerClient.fromEnv();
			if (!builder.uri().getScheme().equals("https") && builder.dockerCertificates() != null) {
				builder.dockerCertificates(null);
			}
			dockerClient = new SpotifyDockerTestClient(builder.build());
		} catch (DockerCertificateException e) {
			String message = String.format("Unable to initialize Docker Client!");
			LOG.error(message, e);
			throw new DockerClientException(message, e);
		}
		return dockerClient;
	}
}
