package com.github.ar4ik4ik.cloudstorage.repository;

import com.github.ar4ik4ik.cloudstorage.exception.StorageException;
import io.minio.GetObjectResponse;
import io.minio.messages.Item;

import java.io.InputStream;
import java.util.List;

public interface S3Repository {

    void uploadObject (String path, String contentType, InputStream inputStream, long objectSize) throws StorageException;

    GetObjectResponse getObject(String path) throws StorageException;

    void createEmptyDirectory(String path) throws StorageException;

    List<Item> getListObjectsByPath(String path) throws StorageException;

    void copyObject(String from, String to, boolean isFolder) throws StorageException;

    void removeObject(String path, boolean isFolder) throws StorageException;
}
