package com.blacklabelops.crow.docker.client.spotify.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.docker.client.DockerClientException;
import com.blacklabelops.crow.docker.client.test.IDockerClientTest;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.RemoveContainerParam;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerConfig.Builder;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ImageInfo;

public class SpotifyDockerTestClient implements IDockerClientTest {
	
public static final Logger LOG = LoggerFactory.getLogger(SpotifyDockerTestClient.class);
	
	private static final String TEST_IMAGE = "busybox:latest";

	private DockerClient dockerClient;
	
	public static int numberOfContainers;

	public static List<String> containers = Collections.synchronizedList(new ArrayList<>());

	public SpotifyDockerTestClient(DockerClient dockerClient) {
		this.dockerClient = dockerClient;
	}	
	
	public DockerClient getDockerClient() {
		return dockerClient;
	}

	@Override
	public String runContainer(){
		if (!checkTestImage()) {
			try {
				dockerClient.pull(TEST_IMAGE);
			} catch (DockerException | InterruptedException e) {
			}
		}
		String id;
		try {
			id = startContainer();
		} catch (DockerException | InterruptedException e) {
			throw new DockerClientException(e);
		}
		containers.add(id);
		return id;
	}

	@Override
	public String runContainer(String name) {
		return this.runContainer(name, null);
	}

	@Override
	public String runContainer(String name, Map<String, String> envs) {
		if (!checkTestImage()) {
			try {
				dockerClient.pull(TEST_IMAGE);
			} catch (DockerException | InterruptedException e) {
			}
		}
		String id;
		try {
			id = startContainer(name, envs);
		} catch (DockerException | InterruptedException e) {
			throw new DockerClientException(e);
		}
		containers.add(id);
		return id;
	}

	@Override
	public boolean checkTestImage() {
		ImageInfo result = null;

		try {
			result = dockerClient.inspectImage(TEST_IMAGE);
		} catch (Exception e) {
		}

		if (result != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void runSomeContainers(int containerAmount) {
		numberOfContainers += containerAmount;
		if (!checkTestImage()) {
			try {
				dockerClient.pull(TEST_IMAGE);
			} catch (DockerException | InterruptedException e) {
			}
		}
		for (int i = 0; i < numberOfContainers; i++) {
			String containerid;
			try {
				containerid = startContainer();
			} catch (DockerException | InterruptedException e) {
				throw new DockerClientException(e);
			}
			containers.add(containerid);
		}
	}

	@Override
	public void deleteContainers() {
		getContainerIds().parallelStream().forEach(c -> {
			stopContainer(c);
		});
	}

	@Override
	public void stopContainer(String c) {
		try {
			dockerClient.removeContainer(c, RemoveContainerParam.forceKill(true), RemoveContainerParam.removeVolumes(true));
		} catch (DockerException | InterruptedException e) {
			throw new DockerClientException(e);
		}
		containers.remove(c);		
	}		

	private String startContainer() throws DockerException, InterruptedException {
		return this.startContainer(null, null);
	}

	private String startContainer(String name, Map<String, String> envs) throws DockerException, InterruptedException {
		String[] command = new String[] { "sh", "-c", "while sleep 1; do :; done" };
		Builder builder = ContainerConfig.builder()
				.image(TEST_IMAGE)
				.cmd(command);
		if (envs != null) {
			builder.env(envsToEnv(envs));
		}
		ContainerConfig containerConfig = builder.build();
		ContainerCreation creation = dockerClient.createContainer(containerConfig);
		if (!StringUtils.isEmpty(name)) {
			dockerClient.renameContainer(creation.id(), name);
		}
		dockerClient.startContainer(creation.id());
		return creation.id();
	}

	private List<String> envsToEnv(Map<String, String> envs) {
		return envs.entrySet().stream().map(e -> e.getKey().concat("=").concat(e.getValue())).collect(Collectors
				.toList());
	}

	@Override
	public List<String> getContainerIds() {
		return new ArrayList<>(containers);
	}
	
}
