package com.github.ar4ik4ik.cloudstorage.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PathUtils {
    public static boolean isFolder(String path) {
        return path.endsWith("/");
    }
    public static String getFilenameForDownload(String path) {
        if (isFolder(path)) {
            return extractNameFromPath(path).replace("/", "") + ".zip";
        } else {
            return extractNameFromPath(path);
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

    public static String extractNameFromPath(String path) {
        if (path.isEmpty()) {
            return "";
        }
        if (path.equals("/")) {
            return "/";
        }

        String tempPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int lastSlashIdx = tempPath.lastIndexOf("/");
        String name;
        if (lastSlashIdx == -1) {
            name = tempPath;
        } else {
            name = tempPath.substring(lastSlashIdx + 1);
        }
        if (path.endsWith("/") && !name.isEmpty()) {
            return name + "/";
        }
        log.info("name={}", name);
        return name;
    }
}
