package com.github.ar4ik4ik.cloudstorage.exception;

public class ObjectNotFoundException extends StorageException {
    public ObjectNotFoundException() {
        super();
    }

    public ObjectNotFoundException(String message) {
        super(message);
    }

    public ObjectNotFoundException(Exception e) {
        super(e);
    }

    public ObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
