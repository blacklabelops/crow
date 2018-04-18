package com.blacklabelops.crow.console.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.junit.rules.ExternalResource;

public class FileAsserter extends ExternalResource {

    private Path file;

    private boolean verbose = false;

    public FileAsserter() {
        super();
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        file = Files.createTempFile("JUnit","FileAsserter");
    }

    @Override
    protected void after() {
        super.after();
        if (verbose) {
            try {
                Files.lines(file).forEach(s -> System.out.println(s));
            } catch (IOException e) {
                throw new RuntimeException("Unable to verbose file!",e);
            }
        }
        try {
            Files.delete(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getFile() {
        return file.toFile();
    }

    public void assertContainsLine(String expected) {
        boolean found = false;
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
            String currentLine;
            while(scanner.hasNextLine()) {
                currentLine = scanner.nextLine();
                if(currentLine.contains(expected)) {
                    found = true;
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to assert file by lines!",e);
        } finally {
        		if (scanner != null) {
        			scanner.close();
        		}
        }
        
        assert found;
    }

    public void assertEquals(String expected) {
        String content = null;
        try {
            content = new String(Files.readAllBytes(file));
        } catch (IOException e) {
            throw new RuntimeException("Unable to assert file contents!",e);
        }
        assert content.contentEquals(expected);
    }

    public boolean isEmpty() {
        return !(Files.exists(file) && file.toFile().length() > 0);
    }

    public long getFilesize() {
        return file.toFile().length();
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
