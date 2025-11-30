package com.github.ar4ik4ik.cloudstorage.service.impl;

import com.github.ar4ik4ik.cloudstorage.aop.PathEnrich;
import com.github.ar4ik4ik.cloudstorage.dao.S3Dao;
import com.github.ar4ik4ik.cloudstorage.exception.ObjectAlreadyExistException;
import com.github.ar4ik4ik.cloudstorage.exception.ObjectNotFoundException;
import com.github.ar4ik4ik.cloudstorage.exception.StorageException;
import com.github.ar4ik4ik.cloudstorage.mapper.ResourceMapper;
import com.github.ar4ik4ik.cloudstorage.model.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.List;

import static com.github.ar4ik4ik.cloudstorage.utils.PathUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final String ROOT_DIRECTORY_PATH_PATTERN_FOR_USER = "user-%s-files/";

    private final DirectoryDownloadStrategy directoryDownloadStrategy;
    private final FileDownloadStrategy fileDownloadStrategy;

    private final S3Dao dao;
    private final ResourceMapper mapper;
    private final ResourceUploader uploader;

    @Override
    public List<ResourceInfoResponseDto> getDirectoryInfo(@PathEnrich String directoryPath) {
        log.info("Getting directory info with path: {}", directoryPath);
        if (!dao.isObjectExists(directoryPath)) {
            throw new ObjectNotFoundException();
        }
        return dao.getListObjectsByPath(directoryPath, false)
                .stream()
                .filter(item -> !item.objectName().equals(directoryPath))
                .map(mapper::toDirectoryInfoDto)
                .toList();
    }

    @Override
    public ResourceInfoResponseDto createDirectory(@PathEnrich String directoryPath) {
        log.info("directory path: {}", directoryPath);
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
            throw new StorageException(e);
        }
    }

    @Override
    public void deleteResource(@PathEnrich String path) {
        if (!dao.isObjectExists(path)) {
            throw new ObjectNotFoundException();
        }

        if (isFolder(path)) {
            dao.removeFolder(path);
        } else {
            dao.removeFile(path);
        }
    }

    @Override
    public StreamingResponseBody downloadResource(@PathEnrich String path) {
        if (!dao.isObjectExists(path)) {
            throw new ObjectNotFoundException();
        }

        return isFolder(path) ? directoryDownloadStrategy.download(path)
                : fileDownloadStrategy.download(path);
    }

    @Override
    public ResourceInfoResponseDto moveResource(@PathEnrich String from, @PathEnrich String to) {
        if (dao.isObjectExists(to)) {
            throw new ObjectAlreadyExistException();
        } else if (!dao.isObjectExists(from)) {
            throw new ObjectNotFoundException();
        }

        boolean folder = isFolder(from);
        if (folder) {
            dao.copyFolder(from, to);
        } else {
            dao.copyFile(from, to);
        }
        try {
            if (folder) {
                dao.removeFolder(from);
            } else {
                dao.removeFile(from);
            }
        } catch (StorageException e) {
            if (folder) {
                dao.removeFolder(to);
            } else {
                dao.removeFile(to);
            }
        }

        long bytesCount = getBytesCount(to);
        return mapper.toMoveResourceDto(from, to, bytesCount);
    }

    @Override
    public List<ResourceInfoResponseDto> searchResourcesByQuery(String query, String rootPath) {
        var allObjects = dao.getListObjectsByPath(rootPath, true);
        var filteredObjects = allObjects.stream()
                .filter(obj -> extractNameFromPath(obj.objectName())
                        .toLowerCase().contains(query.toLowerCase())).toList();
        return filteredObjects.stream()
                .map(mapper::toDirectoryInfoDto)
                .toList();
    }

    @Override
    public List<ResourceInfoResponseDto> uploadResource(MultipartFile[] files, @PathEnrich String uploadingPath) {
        return uploader.upload(files, uploadingPath);
    }

    private long getBytesCount(String filePath) {
        return dao.getObject(filePath).headers().byteCount();
    }
}
