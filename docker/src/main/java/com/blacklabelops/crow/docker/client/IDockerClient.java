package com.blacklabelops.crow.docker.client;

import java.util.List;
import java.util.concurrent.Callable;

public interface IDockerClient {	

	Callable<ExecuteCommandResult> prepareExecuteCommand(DockerJob dockerJob, String[] command);
	
	List<DiscoveredContainer> discoverDockerJobs();
}
