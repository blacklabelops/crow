package com.blacklabelops.crow.executor.docker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.core.command.PullImageResultCallback;

public class DockerTestContainerFactory {
	
	private final DockerClient dockerClient;
	
	public static int numberOfContainers;
	
	public static List<String> containers = new ArrayList<>();
	
	public DockerTestContainerFactory(DockerClient dockerClient) {
		this.dockerClient = dockerClient;
	}
	
	public String runContainer() throws IOException, InterruptedException {
		PullImageResultCallback callback = new PullImageResultCallback();
		dockerClient.pullImageCmd("busybox:latest").exec(callback).awaitCompletion().close();
		return startContainer();
	}
	
	public void runSomeContainers(int containerAmount) throws InterruptedException, IOException {
		numberOfContainers += containerAmount;
		PullImageResultCallback callback = new PullImageResultCallback();
		dockerClient.pullImageCmd("busybox:latest").exec(callback).awaitCompletion().close();
		for (int i=0;i < numberOfContainers;i++) {
			String containerid = startContainer();
			containers.add(containerid);
		}
	}

	public void deleteContainers() {
		containers.parallelStream().forEach(c -> stopContainer(c));
	}

	private void stopContainer(String c) {
		dockerClient.stopContainerCmd(c).withTimeout(0).exec();
		dockerClient.removeContainerCmd(c).withForce(true).withRemoveVolumes(true).exec();
	}

	private String startContainer() {
		CreateContainerResponse container = dockerClient.createContainerCmd("busybox")
				.withCmd("sh", "-c","while sleep 1; do :; done")
				.exec();
		dockerClient.startContainerCmd(container.getId()).exec();
		return container.getId();
	}
	
	public List<String> getContainerIds() {
		return new ArrayList<>(containers);
	}
}
