package com.blacklabelops.crow.executor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputReader implements Runnable {

	final Logger LOG = LoggerFactory.getLogger(OutputReader.class);

	private final Path outputFile;

	private final List<OutputStream> lineConsumer;

	private boolean keepReading;

	public OutputReader(Path file, List<OutputStream> consumer) {
		super();
		outputFile = file;
		lineConsumer = consumer;
	}

	@Override
	public void run() {
		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(outputFile.toFile()))) {
			keepReading = true;
			while (keepReading) {
				lineConsumer.parallelStream().forEach(o -> {
					try {
						IOUtils.copy(inputStream, o);
					} catch (IOException e) {
						LOG.error("Supplying loggers failed.", e);
					}
				});
				waitforNewInput();
				lineConsumer.parallelStream().forEach(o -> {
					try {
						IOUtils.copy(inputStream, o);
					} catch (IOException e) {
						LOG.error("Supplying loggers failed.", e);
					}
				});
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not initialize appender!", e);
		}
	}

	private void waitforNewInput() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			throw new RuntimeException("Cannot initialise wait!", e);
		}
	}

	public void stop() {
		keepReading = false;
	}

}
