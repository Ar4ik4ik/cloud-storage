package com.github.ar4ik4ik.cloudstorage.service.impl;

import com.github.ar4ik4ik.cloudstorage.repository.S3Repository;
import com.github.ar4ik4ik.cloudstorage.service.DownloadStrategy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedInputStream;

@Service
@RequiredArgsConstructor
public class FileDownloadStrategy implements DownloadStrategy {

    private final S3Repository repository;

    @Override
    @SneakyThrows
    public StreamingResponseBody download(String resourcePath) {
        return outputStream -> {
            IOUtils.copy(new BufferedInputStream(repository.getObject(resourcePath)),
                    outputStream, 8192);
        };
    }
}
