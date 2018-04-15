package com.blacklabelops.crow.executor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OutputReader implements Runnable {

    final Logger logger = LoggerFactory.getLogger(OutputReader.class);

    private final Path outputFile;

    private final List<Consumer<String>> lineConsumer;

    private boolean keepReading;

    public OutputReader(Path file, List<Consumer<String>> consumer) {
        super();
        outputFile = file;
        lineConsumer = consumer;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader(outputFile.toFile()))) {
            keepReading = true;
            while (keepReading) {
                readOutputFully(reader);
                waitforNewInput();
                readOutputFully(reader);
            }
        } catch (IOException e) {
          throw new RuntimeException("Could not initialize appender!",e);
        }
    }

    private void waitforNewInput() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException("Cannot initialise wait!",e);
        }
    }

    private void readOutputFully(BufferedReader reader) {
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                pushLineToConsumer(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error trailing file!",e);
        }
    }

    private void pushLineToConsumer(final String line) {
        lineConsumer.forEach(consumer -> consumer.accept(line));
    }

    public void stop() {
        keepReading = false;
    }


}
