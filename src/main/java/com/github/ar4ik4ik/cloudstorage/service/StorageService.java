package com.github.ar4ik4ik.cloudstorage.service;

import com.github.ar4ik4ik.cloudstorage.dto.DirectoryInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.dto.ResourceInfoResponseDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {

    ResourceInfoResponseDto getResourceInfo(String resourcePath);

    void deleteResource(String resourcePath);

    ByteArrayResource downloadResource(String resourcePath);

    ResourceInfoResponseDto moveResource(String from, String to);

    ResourceInfoResponseDto renameResource(String resourcePath, String newResourceName);

    List<ResourceInfoResponseDto> searchResourcesByQuery(String query);

    List<ResourceInfoResponseDto> uploadResource(MultipartFile[] files, String resourcePath);

    List<DirectoryInfoResponseDto> getDirectoryInfo(String directoryPath);

    List<DirectoryInfoResponseDto> createDirectory(String directoryPath);
}
