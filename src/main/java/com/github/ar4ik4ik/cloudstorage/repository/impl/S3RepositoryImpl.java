package com.github.ar4ik4ik.cloudstorage.repository.impl;

import com.github.ar4ik4ik.cloudstorage.exception.*;
import com.github.ar4ik4ik.cloudstorage.repository.S3Repository;
import com.github.ar4ik4ik.cloudstorage.utils.PathUtils;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InternalException;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import io.minio.messages.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static com.github.ar4ik4ik.cloudstorage.dto.ResourceInfoResponseDto.ResourceType.DIRECTORY;
import static com.github.ar4ik4ik.cloudstorage.dto.ResourceInfoResponseDto.ResourceType.FILE;

@Slf4j
@RequiredArgsConstructor
@Repository
public class S3RepositoryImpl implements S3Repository {

    @Value("${minio.bucket}")
    private String bucket;

    private final MinioClient minioClient;

    @Override
    public void uploadObject(String path, String contentType, InputStream inputStream, long objectSize) throws StorageException {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .contentType(contentType)
                    .headers(Map.of("If-None-Match", "*"))
                    .stream(inputStream, objectSize, -1)
                    .tags(Map.of("type", FILE.name()))
                    .build());
        } catch (Exception e) {
            throw mapExceptionToDomain("removeFile", path, e);
        }
    }

    @Override
    public GetObjectResponse getObject(String path) throws StorageException {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw mapExceptionToDomain("removeFile", path, e);
        }
    }

    @Override
    public void createEmptyDirectory(String path) throws StorageException {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .contentType("application/x-directory")
                    .headers(Map.of("If-None-Match", "*"))
                    .tags(Tags.newObjectTags(Map.of("type", DIRECTORY.name())))
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw mapExceptionToDomain("removeFile", path, e);
        }
    }

    @Override
    public List<Item> getListObjectsByPath(String path, boolean recursive) throws StorageException {
        var storageObjectsIterator = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucket)
                .prefix(path)
                .recursive(recursive)
                .build()).iterator();

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(storageObjectsIterator, Spliterator.ORDERED), false)
                .map(item -> {
                    try {
                        log.info("item=={}", item);
                        return item.get();
                    } catch (Exception e) {
                        throw mapExceptionToDomain("removeFile", path, e);
                    }
                })
                .toList();
    }

    @Override
    public void copyObject(String from, String to, boolean isFolder) throws StorageException {
        if (isFolder) {
            copyFolder(from, to);
        } else {
            copyFile(from, to);
        }
    }

    @Override
    public void removeObject(String path, boolean isFolder) throws StorageException {
        if (isFolder) {
            removeFolder(path);
        } else {
            removeFile(path);
        }
    }

    private void removeFile(String path) throws StorageException {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw mapExceptionToDomain("removeFile", path, e);
        }

    }

    private void removeFolder(String path) throws StorageException {
        List<DeleteObject> storageObjectsToRemove = getListObjectsByPath(path, true).stream()
                .map(object -> new DeleteObject(object.objectName()))
                .toList();
        if (storageObjectsToRemove.isEmpty()) {
            log.info("No objects found to remove from folder: {}", path);
            return;
        }

        var deleteResults = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(bucket)
                .objects(storageObjectsToRemove)
                .build());
        for (Result<DeleteError> result : deleteResults) {
            try {
                DeleteError deleteError = result.get();
                log.warn("Failed to delete object: {}\nIn bucket: {}:{}",
                        deleteError.objectName(), deleteError.bucketName(), deleteError.message());
            } catch (Exception e) {
                throw mapExceptionToDomain("removeFolder", path, e);
            }
        }
    }

    private void copyFile(String from, String to) throws StorageException {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucket)
                    .object(to)
                    .source(CopySource.builder()
                            .bucket(bucket)
                            .object(from)
                            .build())
                    .build());
        } catch (Exception e) {
            throw mapExceptionToDomain("copyFile", from, e);
        }
    }

    private void copyFolder(String from, String to) throws StorageException {
        List<Item> storageObjects = getListObjectsByPath(from, true);
        storageObjects.forEach(object -> {
            try {
                minioClient.copyObject(CopyObjectArgs.builder()
                        .bucket(bucket)
                        .object(to.concat(PathUtils.getRelativePath(object.objectName(), from)))
                        .source(CopySource.builder()
                                .bucket(bucket)
                                .object(object.objectName())
                                .build())
                        .build());
            } catch (Exception e) {
                throw mapExceptionToDomain("copyFolder", from, e);
            }
        });
    }

    private StorageException mapExceptionToDomain(String operation, String path, Exception e) {
        log.error("Caught exception during MinIO operation {{}} for path {{}}\nCause: {{}}",
                operation, path, e.getMessage());

        String baseErrorMessage = String.format("Failed MinIO operation {%s} for bucket {%s} and path {%s}",
                operation, bucket, path);

        if (e instanceof ErrorResponseException err) {
            String errorCode = err.errorResponse().code();
            String fullMessage = String.format("Failed MinIO operation {%s} for bucket {%s} and path {%s}",
                    operation, bucket, path);
            return switch (errorCode) {
                case "NoSuchKey" -> new ObjectNotFoundException(fullMessage, err);
                case "PreconditionFailed" -> new ObjectAlreadyExistException(fullMessage, err);
                default -> new StorageException(fullMessage, err);
            };
        } else if (e instanceof InternalException || e instanceof ServerException) {
            return new StorageServiceException(String.format("%s. MinIO internal server error: %s", baseErrorMessage, e.getMessage()), e);
        } else if (e instanceof InvalidKeyException || e instanceof NoSuchAlgorithmException) {
            return new StorageConfigurationException(String.format("%s. MinIO client configuration error: %s", baseErrorMessage, e.getMessage()), e);
        } else if (e instanceof IOException) {
            return new StorageConnectionException(String.format("%s. Network or I/O error: %s", baseErrorMessage, e.getMessage()), e);
        } else if (e instanceof MinioException) {
            return new StorageServiceException(String.format("%s. Generic MinIO client error: %s", baseErrorMessage, e.getMessage()), e);
        } else {
            return new StorageException(String.format("%s. An unexpected error occurred: %s", baseErrorMessage, e.getMessage()), e);
        }
    }
}
