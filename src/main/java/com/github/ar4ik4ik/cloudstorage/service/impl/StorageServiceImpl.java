package com.github.ar4ik4ik.cloudstorage.service.impl;

import com.github.ar4ik4ik.cloudstorage.aop.PathEnrich;
import com.github.ar4ik4ik.cloudstorage.dao.S3Dao;
import com.github.ar4ik4ik.cloudstorage.exception.ObjectAlreadyExistException;
import com.github.ar4ik4ik.cloudstorage.exception.ObjectNotFoundException;
import com.github.ar4ik4ik.cloudstorage.mapper.ResourceMapper;
import com.github.ar4ik4ik.cloudstorage.model.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.service.StorageService;
import com.github.ar4ik4ik.cloudstorage.utils.ResourceInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.github.ar4ik4ik.cloudstorage.utils.PathUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final String ROOT_DIRECTORY_PATH_PATTERN_FOR_USER = "user-%s-files/";

    private final DirectoryDownloadStrategyImpl directoryDownloadStrategyImpl;
    private final FileDownloadStrategyImpl fileDownloadStrategyImpl;

    private final S3Dao dao;
    private final ResourceMapper mapper;

    @Override
    public List<ResourceInfoResponseDto> getDirectoryInfo(@PathEnrich String directoryPath) {
        log.debug("Method calls with directoryPath = {}", directoryPath);
        if (!dao.isObjectExists(directoryPath)) {
            throw new ObjectNotFoundException();
        }
        return dao.getListObjectsByPath(directoryPath, false, false)
                .stream()
                .map(mapper::toDirectoryInfoDto)
                .toList();
    }

    @Override
    public ResourceInfoResponseDto createDirectory(@PathEnrich String directoryPath) {
        if (!dao.isObjectExists(getParentPath(directoryPath, false))) {
            throw new ObjectNotFoundException();
        }
        dao.createEmptyDirectory(directoryPath);
        return mapper.toUploadDirectoryDto(directoryPath);
    }

    @Override
    public void createRootDirectoryForUser(Integer userId) {
        dao.createEmptyDirectory(String.format(ROOT_DIRECTORY_PATH_PATTERN_FOR_USER, userId));
    }

    @Override
    public ResourceInfoResponseDto getResourceInfo(@PathEnrich String directoryPath) {
        try (var obj = dao.getObject(directoryPath)) {
            return mapper.toDto(directoryPath, obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteResource(@PathEnrich String path) {
        if (!dao.isObjectExists(path)) {
            throw new ObjectNotFoundException();
        }

        dao.removeObject(path, isFolder(path));
    }

    @Override
    public StreamingResponseBody downloadResource(@PathEnrich String path) {
        if (!dao.isObjectExists(path)) {
            throw new ObjectNotFoundException();
        }

        return isFolder(path) ? directoryDownloadStrategyImpl.download(path)
                : fileDownloadStrategyImpl.download(path);
    }

    @Override
    public ResourceInfoResponseDto moveResource(@PathEnrich String from, @PathEnrich String to) {
        if (dao.isObjectExists(to)) {
            throw new ObjectAlreadyExistException();
        }

        boolean folder = isFolder(from);

        dao.copyObject(from, to, folder);
        dao.removeObject(from, folder);
        long bytesCount = getBytesCount(to);
        return mapper.toMoveResourceDto(from, to, bytesCount);
    }

    @Override
    public List<ResourceInfoResponseDto> searchResourcesByQuery(String query, String rootPath) {
        var allObjects = dao.getListObjectsByPath(rootPath, true, true);
        var filteredObjects = allObjects.stream()
                .filter(obj -> extractNameFromPath(obj.objectName())
                        .toLowerCase().contains(query.toLowerCase())).toList();
        return filteredObjects.stream()
                .map(mapper::toDirectoryInfoDto)
                .toList();
    }

    @SneakyThrows
    @Override
    public List<ResourceInfoResponseDto> uploadResource(MultipartFile[] files, @PathEnrich String uploadingPath) {
        List<ResourceInfoResponseDto> uploadedResources = new LinkedList<>();
        Set<String> collectedDirectoriesFromInputFiles = collectDirectoriesFromInputFiles(files, uploadingPath);
        uploadDirectories(collectedDirectoriesFromInputFiles, uploadedResources, getRootPath(uploadingPath));

        for (MultipartFile file : files) {
            ResourceInfo resourceInfo = ResourceInfo.create(uploadingPath, file);
            uploadFile(resourceInfo, uploadedResources);
        }
        return uploadedResources;
    }

    private void uploadDirectories(Set<String> collectedDirectoriesFromInputFiles,
                                   List<ResourceInfoResponseDto> resourcesToUpload, String rootPath) {
        collectedDirectoriesFromInputFiles.forEach(directory -> {
                    dao.createEmptyDirectory(rootPath.concat(directory));
                    resourcesToUpload.add(mapper.toUploadDirectoryDto(directory));
                }
        );
    }

    private Set<String> collectDirectoriesFromInputFiles(MultipartFile[] files, String uploadingPath) {
        Set<String> collectedDirectoriesFromInputFiles = new HashSet<>();

        for (MultipartFile file : files) {
            var resourceInfo = ResourceInfo.create(uploadingPath, file);
            String parentDirectoryPathForFile = resourceInfo.getRelativePath();

            Path filePath = Path.of(parentDirectoryPathForFile);
            Path parentPath = filePath.getParent();

            while (parentPath != null) {
                collectedDirectoriesFromInputFiles.add(parentPath.toString()
                        .replace(File.separator, "/")
                        .concat("/"));
                parentPath = parentPath.getParent();
            }
        }
        log.info("Collected directories: {}", collectedDirectoriesFromInputFiles);
        return collectedDirectoriesFromInputFiles;
    }

    private void uploadFile(ResourceInfo resourceInfo, List<ResourceInfoResponseDto> resourcesToUpload) throws IOException {
        try (var inputStream = new BufferedInputStream(resourceInfo.getMultipartFile().getInputStream())) {
            dao.uploadObject(resourceInfo.getFullMinioPath(),
                    resourceInfo.getMultipartFile().getContentType(),
                    inputStream,
                    resourceInfo.getMultipartFile().getSize());

            resourcesToUpload.add(mapper.toUploadFileDto(resourceInfo));
        }
    }

    private long getBytesCount(String filePath) {
        return dao.getObject(filePath).headers().byteCount();
    }
}
