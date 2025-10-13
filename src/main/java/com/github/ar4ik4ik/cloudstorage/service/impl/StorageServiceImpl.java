package com.github.ar4ik4ik.cloudstorage.service.impl;

import com.github.ar4ik4ik.cloudstorage.dto.DirectoryInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.dto.ResourceInfoResponseDto.ResourceType;
import com.github.ar4ik4ik.cloudstorage.props.MinioProperties;
import com.github.ar4ik4ik.cloudstorage.service.StorageService;
import com.github.ar4ik4ik.cloudstorage.utils.ResourceInfo;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public List<DirectoryInfoResponseDto> getDirectoryInfo(String directoryPath) {
        return List.of();
    }

    @Override
    public List<DirectoryInfoResponseDto> createDirectory(String directoryPath) {
        return List.of();
    }

    @Override
    public ResourceInfoResponseDto getResourceInfo(String resourcePath) {
        return null;
    }

    @Override
    public void deleteResource(String resourcePath) {
    }

    @Override
    public ByteArrayResource downloadResource(String resourcePath) {
        return null;
    }

    @Override
    public ResourceInfoResponseDto moveResource(String from, String to) {
        return null;
    }

    @Override
    public ResourceInfoResponseDto renameResource(String resourcePath, String newResourceName) {
        return null;
    }

    @Override
    public List<ResourceInfoResponseDto> searchResourcesByQuery(String query) {
        return List.of();
    }

    @SneakyThrows
    @Override
    public List<ResourceInfoResponseDto> uploadResource(MultipartFile[] files, String resourcePath) {
        String normalizedResourcePath = normalizePath(resourcePath);
        Set<String> createdDirectories = new HashSet<>();
        List<ResourceInfoResponseDto> uploadedResources = new LinkedList<>();

        for (MultipartFile file : files) {
            ResourceInfo resourceInfo = ResourceInfo.create(normalizedResourcePath, file);
            addDirectoryToStorage(resourceInfo.getDirectoryPathForFile(), createdDirectories, uploadedResources);
            addFileToStorage(resourceInfo, uploadedResources);
        }
        return uploadedResources;
    }

    @NotNull
    private static String normalizePath(String resourcePath) {
        if (!resourcePath.endsWith("/")) {
            resourcePath += "/";
        }
        return resourcePath;
    }

    private void addFileToStorage(ResourceInfo resourceInfo, List<ResourceInfoResponseDto> uploadedResources) throws IOException, ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException {
        try (var inputStream = new BufferedInputStream(resourceInfo.getMultipartFile().getInputStream())) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(resourceInfo.getFullMinioPath())
                    .contentType(resourceInfo.getMultipartFile().getContentType())
                    .stream(inputStream, resourceInfo.getMultipartFile().getSize(), -1)
                    .build());

            uploadedResources.add(ResourceInfoResponseDto.builder()
                    .name(resourceInfo.getFilename())
                    .path(resourceInfo.getDirectoryPathForFile())
                    .size(resourceInfo.getMultipartFile().getSize())
                    .type(ResourceType.FILE.name())
                    .build());
        }
    }

    private void addDirectoryToStorage(String directoryPathForFile, Set<String> createdDirectories,
                                       List<ResourceInfoResponseDto> uploadedResources) {
        String[] directories = directoryPathForFile.split("/");

        String currentDirectory = "";

        for (String directory : directories) {
            if (!directory.isEmpty()) {
                currentDirectory = currentDirectory.concat(directory).concat("/");
                if (!createdDirectories.contains(directory)) {
                    uploadedResources.add(ResourceInfoResponseDto.builder()
                            .name(directory)
                            .path(getParentPath(currentDirectory))
                            .name(directory)
                            .size(null)
                            .type(ResourceType.DIRECTORY.name())
                            .build());
                    createdDirectories.add(directory);
                }
            }
        }
    }

    private String getParentPath(String fullDirectoryPath) {
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
}
