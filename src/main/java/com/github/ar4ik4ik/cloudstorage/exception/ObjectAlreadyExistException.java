package com.github.ar4ik4ik.cloudstorage.exception;

public class ObjectAlreadyExistException extends StorageException {
    public ObjectAlreadyExistException() {
        super();
    }

    public ObjectAlreadyExistException(String message) {
        super(message);
    }

    public ObjectAlreadyExistException(Exception e) {
        super(e);
    }

    public ObjectAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
