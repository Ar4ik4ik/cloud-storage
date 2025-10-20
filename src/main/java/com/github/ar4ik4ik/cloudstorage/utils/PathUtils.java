package com.github.ar4ik4ik.cloudstorage.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {
    public static String getFilenameForDownload(String path) {

        Path inputPath = Paths.get(path);

        if (path.endsWith("/")) {
            return inputPath.getFileName().toString().concat(".zip");
        } else {
            return inputPath.getFileName().toString();
        }
    }

    public static String getRelativePath(String fullObjectPath, String fromDirectoryPath) {
        return fullObjectPath.substring(fromDirectoryPath.length());
    }

    public static String getParentPath(String fullDirectoryPath) {
        if (fullDirectoryPath == null || fullDirectoryPath.isEmpty() || fullDirectoryPath.equals("/")) {
            return "/";
        }
        String pathWithoutTrailingSlash = fullDirectoryPath.endsWith("/")
                ? fullDirectoryPath.substring(0, fullDirectoryPath.length() - 1)
                : fullDirectoryPath;
        int lastSlashIdx = pathWithoutTrailingSlash.lastIndexOf("/");
        if (lastSlashIdx != -1) {
            return pathWithoutTrailingSlash.substring(0, lastSlashIdx + 1);
        }
        return "/";
    }

    public static String getDirectoryFromFullPath(String fullDirectoryPath) {
        return Paths.get(fullDirectoryPath).getFileName().toString().concat("/");
    }
}
