package com.blacklabelops.crow.executor;

public class ExecutorException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ExecutorException() {
    }

    public ExecutorException(String message) {
        super(message);
    }

    public ExecutorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExecutorException(Throwable cause) {
        super(cause);
    }

    public ExecutorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
