package com.blacklabelops.crow.docker.client.spotify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.docker.client.DiscoveredContainer;
import com.blacklabelops.crow.docker.client.DockerClientException;
import com.blacklabelops.crow.docker.client.DockerJob;
import com.blacklabelops.crow.docker.client.ExecuteCommandResult;
import com.blacklabelops.crow.docker.client.IDockerClient;
import com.cronutils.utils.StringUtils;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ExecCreateParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.ExecState;

public class SpotifyDockerClient implements IDockerClient {
	
	public static final Logger LOG = LoggerFactory.getLogger(SpotifyDockerClient.class);
	
	private DockerClient dockerClient;		

	public SpotifyDockerClient(DockerClient dockerClient) {
		this.dockerClient = dockerClient;
	}
	
	private ExecCreation prepareExecution(DockerJob dockerJob, String[] command) {
		List<ExecCreateParam> parameters = new ArrayList<>();
		parameters.add(DockerClient.ExecCreateParam.attachStderr());
		parameters.add(DockerClient.ExecCreateParam.attachStdout());
		if (dockerJob.getWorkingDir().isPresent()) {
			parameters.add(new ExecCreateParam("WorkingDir", dockerJob.getWorkingDir().get()));
		}
		ExecCreateParam[] executionParams = parameters.toArray(new ExecCreateParam[parameters.size()]);
		ExecCreation execCreation = null;
		try {
			execCreation = dockerClient.execCreate(dockerJob.getContainerId(), command, executionParams);
		} catch (DockerException | InterruptedException e) {
			String message = String.format("Execution creation for job %s failed!",
					dockerJob.getJobLabel());
			LOG.error(message, e);
			throw new DockerClientException(message, e);
		}
		return execCreation;
	}
	
	public Callable<ExecuteCommandResult> prepareExecuteCommand(DockerJob dockerJob, String[] command) {
		Callable<ExecuteCommandResult> task = new Callable<ExecuteCommandResult>() {
			public ExecuteCommandResult call() {
				boolean timedOut = true;
				Optional<Integer> returnCode = Optional.empty();
				LogStream output = null;
				ExecCreation processBuilder = prepareExecution(dockerJob, command);
				try {
					try {						
						output = dockerClient.execStart(processBuilder.id());
						output.attach(dockerJob.getOutput(), dockerJob.getErrorOutput(), false);
					} catch (Exception e) {
						// Problem with Unix socket and spotify docker-client library
						// Must ignore error. Happens around one of three executions
						if (!e.getMessage().contains("Connection reset by peer")) {
							throw e;
						} else {
							LOG.debug("Execution error ignored.", e);
						}
					}
					ExecState state = dockerClient.execInspect(processBuilder.id());
					returnCode = Optional.ofNullable(state.exitCode());
				} catch (DockerException | InterruptedException | IOException e) {
					String message = String.format("Error executing job %s !",
							dockerJob.getJobLabel());
					LOG.error(message, e);
					throw new DockerClientException(message, e);
				} finally {
					if (output != null) {
						output.close();
					}
				}
				timedOut = false;
				return ExecuteCommandResult.builder().timedOut(timedOut).returnCode(returnCode).build();
			}
		};
		return task;
	}

	@Override
	public List<DiscoveredContainer> discoverDockerJobs() {
		List<Container> containers = new ArrayList<Container>();
		
		try {
			containers = dockerClient.listContainers();
		} catch (DockerException | InterruptedException e) {
			LOG.error("Error listing containers!", e);
		}
		
		return containers.stream().map(c -> {
			try {
				ContainerInfo inf = dockerClient.inspectContainer(c.id());
				DiscoveredContainer foundJobs = inspectionToJobConfiguration(inf);
				return foundJobs;
			} catch (DockerException | InterruptedException e) {
				LOG.error("Error inspecting container {}!", c.id());
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());		
	}
	
	private DiscoveredContainer inspectionToJobConfiguration(ContainerInfo inf) {
		DiscoveredContainer container = createContainer(inf);
		Optional<Map<String, String>> envs = Optional.empty();
		if (inf.config() != null && inf.config().env() != null && !inf.config().env().isEmpty()) {
			envs = Optional.of(envsToMap(inf.config().env()));
			container = container.withEnvs(envs);
		}
		Optional<Map<String, String>> props = Optional.empty();
		if (inf.config() != null && inf.config().labels() != null && !inf.config().labels().isEmpty()) {
			props = Optional.of(inf.config().labels());
			container = container.withProps(props);
		}
		return container;
	}
	
	private DiscoveredContainer createContainer(ContainerInfo inf) {
		DiscoveredContainer cf = DiscoveredContainer.builder().build();
		if (!StringUtils.isEmpty(inf.name())) {
			cf = cf.withContainerName(inf.name());
		}
		if (!StringUtils.isEmpty(inf.id())) {
			cf = cf.withContainerId(inf.id());
		}
		return cf;
	}	

	private Map<String, String> envsToMap(List<String> envs) {
		Map<String, String> map = new HashMap<>();
		if (envs != null && !envs.isEmpty()) {
			for (String env : envs) {
				fromStringToMap(env, map);
			}
		}
		return map;
	}
	
	private void fromStringToMap(String env, Map<String, String> map) {
		String[] parts = env.split("=", 2);
		if (parts != null && parts.length == 2) {
			map.put(parts[0], parts[1]);
		}
	}
}
