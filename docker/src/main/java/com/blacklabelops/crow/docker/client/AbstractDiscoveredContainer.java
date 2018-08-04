package com.blacklabelops.crow.docker.client;

import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

@Value.Immutable
@ImmutableStyle
public abstract class AbstractDiscoveredContainer {
	
	public abstract Optional<String> getContainerName();
	
	public abstract Optional<String> getContainerId();
	
	public abstract Optional<Map<String, String>> getEnvs();
	
	public abstract Optional<Map<String, String>> getProps();	
}
