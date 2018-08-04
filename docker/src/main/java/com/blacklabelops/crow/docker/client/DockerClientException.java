package com.blacklabelops.crow.docker.client;

public class DockerClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DockerClientException() {
    }

    public DockerClientException(String message) {
        super(message);
    }

    public DockerClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public DockerClientException(Throwable cause) {
        super(cause);
    }

    public DockerClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
