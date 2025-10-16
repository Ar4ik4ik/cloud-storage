package com.github.ar4ik4ik.cloudstorage.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceUtils {
    public static String getFilename(String path) {

        Path inputPath = Paths.get(path);

        if (path.endsWith("/")) {
            return inputPath.getFileName().toString().concat(".zip");
        } else {
            return inputPath.getFileName().toString();
        }
    }
}
