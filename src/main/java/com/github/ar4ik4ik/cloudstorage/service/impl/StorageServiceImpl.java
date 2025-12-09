package com.github.ar4ik4ik.cloudstorage.service.impl;

import com.github.ar4ik4ik.cloudstorage.aop.PathEnrich;
import com.github.ar4ik4ik.cloudstorage.dao.S3Dao;
import com.github.ar4ik4ik.cloudstorage.exception.ObjectAlreadyExistException;
import com.github.ar4ik4ik.cloudstorage.exception.ObjectNotFoundException;
import com.github.ar4ik4ik.cloudstorage.exception.StorageException;
import com.github.ar4ik4ik.cloudstorage.mapper.ResourceMapper;
import com.github.ar4ik4ik.cloudstorage.model.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.service.StorageService;
import com.github.ar4ik4ik.cloudstorage.utils.PathUtils;
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

    private static final String ROOT_DIRECTORY_PATH_PATTERN_FOR_USER = "user-%s-files/";
    private static final boolean RECURSIVE_SEARCH = true;
    private static final boolean FLAT_SEARCH = false;
    public static final boolean WITH_ROOT_PATH = false;

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
        return dao.getListObjectsByPath(directoryPath, FLAT_SEARCH)
                .stream()
                .filter(item -> !item.objectName().equals(directoryPath))
                .map(mapper::toDirectoryInfoDto)
                .toList();
    }

    @Override
    public ResourceInfoResponseDto createDirectory(@PathEnrich String directoryPath) {
        log.info("directory path: {}", directoryPath);
        if (!dao.isObjectExists(getParentPath(directoryPath, WITH_ROOT_PATH))) {
            throw new ObjectNotFoundException();
        }
        dao.createEmptyDirectory(directoryPath);
        return mapper.toUploadDirectoryDto(directoryPath);
    }

    @Override
    public void createRootDirectoryForUser(Integer id) {
        dao.createEmptyDirectory(String.format(ROOT_DIRECTORY_PATH_PATTERN_FOR_USER, id));
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

        performRemove(path, isFolder(path));
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
        } else if (!dao.isObjectExists(from) || !dao.isObjectExists(getParentPath(to, false))) {
            throw new ObjectNotFoundException();
        } else if (PathUtils.isAncestorOrSelf(from, to)) {
            throw new IllegalArgumentException("Cannot move folder into one of its subfolder");
        }

        boolean isDirectory = isFolder(from);
        performCopy(from, to, isDirectory);
        try {
            performRemove(from, isDirectory);
        } catch (StorageException e) {
            // Попытка ручного отката, если не удалось удалить источник
            performRemove(to, isDirectory);
            log.error("Error moving resource, rolling back", e);
            throw e;
        }

        long bytesCount = getBytesCount(to);
        return mapper.toMoveResourceDto(to, bytesCount);
    }

    @Override
    public List<ResourceInfoResponseDto> searchResourcesByQuery(String query, String rootPath) {
        var searchQueryNormalized = query.toLowerCase();
        var allObjects = dao.getListObjectsByPath(rootPath, RECURSIVE_SEARCH);
        return allObjects.stream()
                .filter(obj -> extractNameFromPath(obj.objectName())
                        .toLowerCase().contains(searchQueryNormalized))
                .map(mapper::toDirectoryInfoDto)
                .toList();
    }

    @Override
    public List<ResourceInfoResponseDto> uploadResource(MultipartFile[] files, @PathEnrich String uploadingPath) {
        return uploader.upload(files, uploadingPath);
    }

    private void performRemove(String from, boolean isDirectory) {
        if (isDirectory) {
            dao.removeFolder(from);
        } else {
            dao.removeFile(from);
        }
    }

    private void performCopy(String from, String to, boolean isDirectory) {
        if (isDirectory) {
            dao.copyFolder(from, to);
        } else {
            dao.copyFile(from, to);
        }
    }

    private long getBytesCount(String filePath) {
        try (var obj = dao.getObject(filePath)) {
            return obj.headers().byteCount();
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }
}
