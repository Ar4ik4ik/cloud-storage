package com.github.ar4ik4ik.cloudstorage.repository.impl;

import com.github.ar4ik4ik.cloudstorage.exception.StorageException;
import com.github.ar4ik4ik.cloudstorage.repository.S3Repository;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.minio.messages.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static com.github.ar4ik4ik.cloudstorage.dto.ResourceInfoResponseDto.ResourceType.DIRECTORY;
import static com.github.ar4ik4ik.cloudstorage.dto.ResourceInfoResponseDto.ResourceType.FILE;

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
                    .build());
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new StorageException("Failed to upload data in storage, input data: key={%s}, contentType={%s}. Cause: %s"
                    .formatted(path, "application/x-directory", e.getMessage()));
        }
    }
}
