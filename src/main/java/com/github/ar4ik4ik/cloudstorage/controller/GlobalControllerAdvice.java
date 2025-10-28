package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.exception.UserAlreadyExistsException;
import com.github.ar4ik4ik.cloudstorage.model.dto.MessageDto;
import com.github.ar4ik4ik.cloudstorage.exception.ObjectAlreadyExistException;
import com.github.ar4ik4ik.cloudstorage.exception.ObjectNotFoundException;
import com.github.ar4ik4ik.cloudstorage.exception.StorageException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(ObjectAlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public MessageDto handleObjectAlreadyExist(ObjectAlreadyExistException ex) {
        return new MessageDto(ex.getMessage());
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public MessageDto handleObjectNotFound(ObjectNotFoundException ex) {
        return new MessageDto(ex.getMessage());
    }

    @ExceptionHandler(StorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageDto handleInternalException(StorageException ex) {
        return new MessageDto(ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageDto handleValidationException(ConstraintViolationException ex) {
        return new MessageDto(ex.getMessage());
    }
}
