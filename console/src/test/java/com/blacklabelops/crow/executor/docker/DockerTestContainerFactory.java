package com.blacklabelops.crow.executor.docker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.RemoveContainerParam;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ImageInfo;

public class DockerTestContainerFactory {

	private static final String TEST_IMAGE = "busybox:latest";

	private final DockerClient dockerClient;

	public static int numberOfContainers;

	public static List<String> containers = Collections.synchronizedList(new ArrayList<>());

	public DockerTestContainerFactory(DockerClient dockerClient) {
		this.dockerClient = dockerClient;
	}

	public String runContainer() throws DockerException, InterruptedException {
		if (!checkTestImage()) {
			try {
				dockerClient.pull(TEST_IMAGE);
			} catch (DockerException | InterruptedException e) {
			}
		}
		String id = startContainer();
		containers.add(id);
		return id;
	}

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

	public void runSomeContainers(int containerAmount) throws DockerException, InterruptedException {
		numberOfContainers += containerAmount;
		if (!checkTestImage()) {
			try {
				dockerClient.pull(TEST_IMAGE);
			} catch (DockerException | InterruptedException e) {
			}
		}
		for (int i = 0; i < numberOfContainers; i++) {
			String containerid = startContainer();
			containers.add(containerid);
		}
	}

	public void deleteContainers() {
		getContainerIds().parallelStream().forEach(c -> {
			try {
				stopContainer(c);
			} catch (DockerException e) {
			} catch (InterruptedException e) {
			}
		});
	}

	private void stopContainer(String c) throws DockerException, InterruptedException {
		dockerClient.removeContainer(c, RemoveContainerParam.forceKill(true), RemoveContainerParam.removeVolumes(true));
	}

	private String startContainer() throws DockerException, InterruptedException {
		String[] command = new String[] { "sh", "-c", "while sleep 1; do :; done" };
		ContainerConfig containerConfig = ContainerConfig.builder()
				.image(TEST_IMAGE)
				.cmd(command)
				.build();
		ContainerCreation creation = dockerClient.createContainer(containerConfig);
		dockerClient.startContainer(creation.id());
		return creation.id();
	}

	public List<String> getContainerIds() {
		return new ArrayList<>(containers);
	}
}
