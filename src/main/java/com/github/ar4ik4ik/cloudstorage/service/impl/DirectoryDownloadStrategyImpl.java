package com.github.ar4ik4ik.cloudstorage.service.impl;

import com.github.ar4ik4ik.cloudstorage.exception.StorageException;
import com.github.ar4ik4ik.cloudstorage.repository.S3Repository;
import com.github.ar4ik4ik.cloudstorage.service.DownloadStrategy;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectoryDownloadStrategyImpl implements DownloadStrategy {

    private final S3Repository repository;

    @Override
    public StreamingResponseBody download(String resourcePath) {
        return outputStream -> {
            var storageItems = repository.getListObjectsByPath(resourcePath, true);

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(outputStream))) {
                for (Item item: storageItems) {
                    var storageObject = repository.getObject(item.objectName());
                    try (BufferedInputStream inputStream = new BufferedInputStream(storageObject)) {
                        var zipEntry = new ZipEntry(item.objectName());

                        zipOutputStream.putNextEntry(zipEntry);
                        IOUtils.copy(inputStream, zipOutputStream, 8192);
                        zipOutputStream.closeEntry();
                    }
                }
            } catch (StorageException e) {
                log.warn("Failed to retrieve data from path={}\nCause:{}", resourcePath, e.getMessage());
            } catch (IOException e) {
                log.error("Failed to zip data from path={}\nCause:{}", resourcePath, e.getMessage());
                throw e;
            }
        };
    }
}
