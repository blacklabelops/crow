package com.blacklabelops.crow.executor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileAccessor {

    public FileAccessor() {
        super();
    }

    public Path createTempFile(String prefix, String suffix) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile(prefix, suffix);
        } catch (IOException e) {
            String msg = String.format("Could not create tempfile with prefix %s and suffix %s", prefix, suffix);
            throw new ExecutorException(msg,e);
        }
        return tempFile;
    }

    public void deleteFile(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            String msg = String.format("Could not delete Outputfile %s", path);
            throw new ExecutorException(msg,e);
        }
    }
}
