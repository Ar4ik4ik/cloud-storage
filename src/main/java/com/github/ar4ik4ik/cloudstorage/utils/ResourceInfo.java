package com.github.ar4ik4ik.cloudstorage.utils;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

@Getter
public class ResourceInfo {

    private String relativePath;
    private String fullMinioPath;
    private String parentDirectoryPathForFile;
    private String filename;
    private MultipartFile multipartFile;

    public static ResourceInfo create(String uploadingPath, MultipartFile file) {
        ResourceInfo info = new ResourceInfo();
        info.multipartFile = file;
        info.relativePath = Objects.requireNonNull(file.getOriginalFilename());
        info.relativePath = Paths.get(info.relativePath).normalize().toString().replace(File.separator, "/");
        info.fullMinioPath = uploadingPath + info.relativePath;

        int lastSlashIdx = info.fullMinioPath.lastIndexOf("/");
        int firstSlashIdx = info.fullMinioPath.indexOf("/");
        if (lastSlashIdx != -1) {
            info.parentDirectoryPathForFile = info.fullMinioPath.substring(firstSlashIdx + 1, lastSlashIdx + 1);
            info.filename = info.fullMinioPath.substring(lastSlashIdx + 1);
        } else {
            info.parentDirectoryPathForFile = uploadingPath;
            info.filename = info.fullMinioPath.substring(uploadingPath.length());
        }
        return info;
    }
}
