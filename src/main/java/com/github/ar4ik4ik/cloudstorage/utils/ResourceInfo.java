package com.github.ar4ik4ik.cloudstorage.utils;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

@Getter
public class ResourceInfo {

    private String originalFilename;
    private String normalizedFilename;
    private String fullMinioPath;
    private String directoryPathForFile;
    private String filename;
    private MultipartFile multipartFile;

    public static ResourceInfo create(String normalizedResourcePath, MultipartFile file) {
        ResourceInfo info = new ResourceInfo();
        info.multipartFile = file;
        info.originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        info.normalizedFilename = Paths.get(info.originalFilename).normalize().toString().replace(File.separator, "/");
        info.fullMinioPath = normalizedResourcePath + info.normalizedFilename;

        int lastSlashIdx = info.fullMinioPath.lastIndexOf("/");
        if (lastSlashIdx != -1) {
            info.directoryPathForFile = info.fullMinioPath.substring(0, lastSlashIdx + 1);
            info.filename = info.fullMinioPath.substring(lastSlashIdx + 1);
        } else {
            info.directoryPathForFile = normalizedResourcePath;
            info.filename = info.fullMinioPath.substring(normalizedResourcePath.length());
        }
        return info;
    }
}
