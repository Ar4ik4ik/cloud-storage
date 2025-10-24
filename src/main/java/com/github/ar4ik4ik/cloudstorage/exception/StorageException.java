package com.github.ar4ik4ik.cloudstorage.exception;

public class StorageException extends RuntimeException {
    public StorageException() {
        super();
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(Exception e) {
        super(e);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
