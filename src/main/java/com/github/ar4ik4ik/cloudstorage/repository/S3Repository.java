package com.github.ar4ik4ik.cloudstorage.repository;

import com.github.ar4ik4ik.cloudstorage.exception.StorageException;
import io.minio.GetObjectResponse;

import java.io.InputStream;

public interface S3Repository {

    void uploadObject (String path, String contentType, InputStream inputStream, long objectSize) throws StorageException;

    GetObjectResponse getObject(String path) throws StorageException;

    void createEmptyDirectory(String path) throws StorageException;
}
