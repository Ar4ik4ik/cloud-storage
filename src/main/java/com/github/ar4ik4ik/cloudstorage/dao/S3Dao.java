package com.github.ar4ik4ik.cloudstorage.dao;

import com.github.ar4ik4ik.cloudstorage.exception.StorageException;
import io.minio.GetObjectResponse;
import io.minio.messages.Item;

import java.io.InputStream;
import java.util.List;

public interface S3Dao {

    void uploadObject (String path, String contentType, InputStream inputStream, long objectSize) throws StorageException;

    GetObjectResponse getObject(String path) throws StorageException;

    void createEmptyDirectory(String path) throws StorageException;

    List<Item> getListObjectsByPath(String path, boolean recursive) throws StorageException;

    boolean isObjectExists(String path);

    void removeFile(String path) throws StorageException;

    void removeFolder(String path) throws StorageException;

    void copyFile(String from, String to) throws StorageException;

    void copyFolder(String from, String to) throws StorageException;
}
