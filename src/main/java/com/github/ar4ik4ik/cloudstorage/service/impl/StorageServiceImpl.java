package com.github.ar4ik4ik.cloudstorage.service.impl;

import com.github.ar4ik4ik.cloudstorage.dto.DirectoryInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.repository.S3Repository;
import com.github.ar4ik4ik.cloudstorage.service.StorageService;
import com.github.ar4ik4ik.cloudstorage.utils.ResourceInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static com.github.ar4ik4ik.cloudstorage.dto.ResourceInfoResponseDto.ResourceType.DIRECTORY;
import static com.github.ar4ik4ik.cloudstorage.dto.ResourceInfoResponseDto.ResourceType.FILE;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final DirectoryDownloadStrategy directoryDownloadStrategy;
    private final FileDownloadStrategy fileDownloadStrategy;

    private final S3Repository repository;

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
    public StreamingResponseBody downloadResource(String resourcePath) {
        String normalizedOriginalResourcePath = Paths.get(resourcePath).normalize().toString();

        // TODO: think about extend checking directory or not with tag comparing (if suffix "/" always mean directory - don't need it)
        // TODO: exception handling keyDoesNotExistsException
        return resourcePath.endsWith("/") ? directoryDownloadStrategy.download(normalizedOriginalResourcePath)
                : fileDownloadStrategy.download(normalizedOriginalResourcePath);
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
        String normalizedOriginalResourcePath = Paths.get(resourcePath).normalize().toString();
        List<ResourceInfoResponseDto> uploadedResources = new LinkedList<>();
        // if already exist do nothing, same logic if no directories in the path
        addDirectoriesToStorageRecursive(normalizedOriginalResourcePath, uploadedResources);

        for (MultipartFile file : files) {
            ResourceInfo resourceInfo = ResourceInfo.create(resourcePath, file);
            addFileToStorage(resourceInfo, uploadedResources);
        }
        return uploadedResources;
    }

    private void addFileToStorage(ResourceInfo resourceInfo, List<ResourceInfoResponseDto> uploadedResources) throws IOException {
        try (var inputStream = new BufferedInputStream(resourceInfo.getMultipartFile().getInputStream())) {
            // TODO: need keyAlreadyExist exception catching
            repository.uploadObject(resourceInfo.getFullMinioPath(),
                    resourceInfo.getMultipartFile().getContentType(),
                    inputStream,
                    resourceInfo.getMultipartFile().getSize());

            uploadedResources.add(ResourceInfoResponseDto.builder()
                    .name(resourceInfo.getFilename())
                    .path(resourceInfo.getDirectoryPathForFile())
                    .size(resourceInfo.getMultipartFile().getSize())
                    .type(FILE.name())
                    .build());
        }
    }

    private void addDirectoriesToStorageRecursive(String directoryPathForFile, List<ResourceInfoResponseDto> uploadedResources) {

        Set<String> createdDirectories = new HashSet<>();
        String[] directories = directoryPathForFile.split("/");
        String currentDirectory = "";
        log.info("directories={}", Arrays.stream(directories).toList());

        for (String directory : directories) {
            if (!directory.isEmpty()) {
                currentDirectory = currentDirectory.concat(directory).concat("/");
                if (!createdDirectories.contains(directory)) {
                    repository.createEmptyDirectory(currentDirectory); // TODO: need keyAlreadyExistException handling if directory already exists

                    uploadedResources.add(ResourceInfoResponseDto.builder()
                            .name(directory)
                            .path(getParentPath(currentDirectory))
                            .name(directory)
                            .size(0L) // directories always would be with type=application/x-directory and size=0 (S3 specifically logic)
                            .type(DIRECTORY.name())
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
