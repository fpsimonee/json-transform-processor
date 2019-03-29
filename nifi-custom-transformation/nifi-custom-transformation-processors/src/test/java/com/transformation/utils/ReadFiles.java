package com.transformation.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;

/**
 * Read input or output file test
 */
public class ReadFiles {
    public static final String PROJECT_PATH = System.getProperty("user.dir");

    /**
     * Get file contents
     */
    public byte[] get(String path) throws IOException {
        FileInputStream fis = new FileInputStream(PROJECT_PATH + path);
        return IOUtils.toByteArray(fis);
    }
}