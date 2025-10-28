package com.github.ar4ik4ik.cloudstorage.service;

import com.github.ar4ik4ik.cloudstorage.model.dto.ResourceInfoResponseDto;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

public interface StorageService {

    ResourceInfoResponseDto getResourceInfo(String resourcePath);

    void deleteResource(String resourcePath);

    StreamingResponseBody downloadResource(String resourcePath);

    ResourceInfoResponseDto moveResource(String from, String to);

    List<ResourceInfoResponseDto> searchResourcesByQuery(String query);

    List<ResourceInfoResponseDto> uploadResource(MultipartFile[] files, String resourcePath);

    List<ResourceInfoResponseDto> getDirectoryInfo(String directoryPath);

    ResourceInfoResponseDto createDirectory(String directoryPath);
}
