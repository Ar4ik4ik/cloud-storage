package com.github.ar4ik4ik.cloudstorage.service.impl;

import com.github.ar4ik4ik.cloudstorage.dao.S3Dao;
import com.github.ar4ik4ik.cloudstorage.exception.ObjectAlreadyExistException;
import com.github.ar4ik4ik.cloudstorage.mapper.ResourceMapper;
import com.github.ar4ik4ik.cloudstorage.model.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.utils.ResourceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.ar4ik4ik.cloudstorage.utils.PathUtils.getRootPath;

@Slf4j
@Component
@RequiredArgsConstructor
class ResourceUploader {

    private final S3Dao dao;
    private final ResourceMapper mapper;


    public List<ResourceInfoResponseDto> upload(MultipartFile[] files, String uploadingPath) {
        List<ResourceInfo> resourceInfoList = new ArrayList<>(files.length);
        for (MultipartFile file : files) {
            ResourceInfo resourceInfo = ResourceInfo.create(uploadingPath, file);
            if (dao.isObjectExists(resourceInfo.getFullMinioPath())) {
                throw new ObjectAlreadyExistException();
            }
            resourceInfoList.add(resourceInfo);
        }

        List<ResourceInfoResponseDto> uploadedResources = new ArrayList<>(resourceInfoList.size());

        Set<String> collectedDirectoriesFromInputFiles = collectDirectoriesFromInputFiles(resourceInfoList);
        uploadDirectories(collectedDirectoriesFromInputFiles, uploadedResources, getRootPath(uploadingPath));

        for (ResourceInfo resourceInfo : resourceInfoList) {
            try {
                uploadFile(resourceInfo, uploadedResources);
            } catch (IOException e) {
                // При ошибке загрузки одного файла процесс не прерывается,
                // но при наличии дубликатов операция отменяется полностью до начала загрузки
                log.warn("Can't upload file: {}\nCause: {}", resourceInfo, e.toString());
            }
        }
        return uploadedResources;
    }

    // Создаем структуру каталогов перед загрузкой файлов, чтобы гарантировать наличие родительских путей (необходимо при поиске)
    private void uploadDirectories(Set<String> collectedDirectoriesFromInputFiles,
                                   List<ResourceInfoResponseDto> resourcesToUpload, String rootPath) {
        collectedDirectoriesFromInputFiles.forEach(directory -> {
                    dao.createEmptyDirectory(rootPath.concat(directory));
                    resourcesToUpload.add(mapper.toUploadDirectoryDto(directory));
                }
        );
    }

    private Set<String> collectDirectoriesFromInputFiles(List<ResourceInfo> resourceInfoList) {
        Set<String> collectedDirectoriesFromInputFiles = new HashSet<>();

        for (ResourceInfo resourceInfo : resourceInfoList) {

            Path filePath = Path.of(resourceInfo.getRelativePath());
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
}
