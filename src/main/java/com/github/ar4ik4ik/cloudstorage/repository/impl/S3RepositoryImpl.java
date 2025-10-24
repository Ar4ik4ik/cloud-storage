package com.github.ar4ik4ik.cloudstorage.repository.impl;

import com.github.ar4ik4ik.cloudstorage.exception.StorageException;
import com.github.ar4ik4ik.cloudstorage.repository.S3Repository;
import com.github.ar4ik4ik.cloudstorage.utils.PathUtils;
import io.minio.*;
import io.minio.errors.MinioException;
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
                    .stream(inputStream, objectSize, -1)
                    .tags(Map.of("type", FILE.name()))
                    .build());
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new StorageException("Failed to upload data in storage, input data: path={%s}, contentType={%s}. Cause: %s"
                    .formatted(path, contentType, e.getMessage()));
        }
    }

    @Override
    public GetObjectResponse getObject(String path) throws StorageException {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build());
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new StorageException("Failed to retrieve data from storage, input data: path={%s}. Cause: %s"
                    .formatted(path, e.getMessage()));
        }
    }

    @Override
    public void createEmptyDirectory(String path) throws StorageException {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .contentType("application/x-directory")
                    .tags(Tags.newObjectTags(Map.of("type", DIRECTORY.name())))
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new StorageException("Failed to upload data in storage, input data: key={%s}, contentType={%s}. Cause: %s"
                    .formatted(path, "application/x-directory", e.getMessage()));
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
                    } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                        throw new StorageException("Failed to get data from storage, input data: key={%s}. Cause: %s"
                                .formatted(path, e.getMessage()));
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

    private void removeFile(String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build());
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.warn("Failed to delete object: {}\nIn bucket: {}:{}",
                    path, bucket, e.getMessage());
            throw new StorageException(e.getMessage());
        }

    }

    private void removeFolder(String path) {
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
                log.error("Failed to retrieve delete results for {}\nCause: {}", path, e.getMessage());
            }
        }
    }

    private void copyFile(String from, String to) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucket)
                    .object(to)
                    .source(CopySource.builder()
                            .bucket(bucket)
                            .object(from)
                            .build())
                    .build());
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new StorageException("Failed to copy data in storage, input data: from={%s}, to={%s}. Cause: %s"
                    .formatted(from, to, e.getMessage()));
        }
    }

    private void copyFolder(String from, String to) {
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
            } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                throw new StorageException("Failed to copy data in storage, input data: from={%s}, to={%s}. Cause: %s"
                        .formatted(from, to, e.getMessage()));
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
