package com.blacklabelops.crow.docker.client.test;

import java.util.List;
import java.util.Map;

public interface IDockerClientTest {

	List<String> getContainerIds();

	void deleteContainers();

	void runSomeContainers(int containerAmount);

	boolean checkTestImage();

	String runContainer(String name, Map<String, String> envs);

	String runContainer(String name);

	String runContainer();

	void stopContainer(String c);	

}
