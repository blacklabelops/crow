package com.blacklabelops.crow.docker.client;

import java.io.OutputStream;
import java.util.Optional;

import org.immutables.value.Value;

@Value.Immutable
@ImmutableStyle
public abstract class AbstractDockerJob {
		
	public abstract String getJobLabel();
	
	public abstract String getContainerId();
	
	public abstract Optional<String> getWorkingDir();
	
	public abstract OutputStream getOutput();
	
	public abstract OutputStream getErrorOutput();
}
