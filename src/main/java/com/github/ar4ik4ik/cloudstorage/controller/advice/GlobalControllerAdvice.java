package com.github.ar4ik4ik.cloudstorage.controller.advice;

import com.github.ar4ik4ik.cloudstorage.exception.ObjectAlreadyExistException;
import com.github.ar4ik4ik.cloudstorage.exception.ObjectNotFoundException;
import com.github.ar4ik4ik.cloudstorage.exception.StorageException;
import com.github.ar4ik4ik.cloudstorage.exception.UserAlreadyExistsException;
import com.github.ar4ik4ik.cloudstorage.model.dto.MessageDto;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Hidden
@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(ObjectAlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public MessageDto handleObjectAlreadyExist(ObjectAlreadyExistException ex) {
        return new MessageDto("Object in target path already exists");
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public MessageDto handleObjectNotFound(ObjectNotFoundException ex) {
        return new MessageDto("Target path not found");
    }

    @ExceptionHandler(StorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageDto handleInternalException(StorageException ex) {
        return new MessageDto(ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageDto handleValidationException(ConstraintViolationException ex) {
        StringBuilder resultString = new StringBuilder();
        ex.getConstraintViolations()
                .forEach(violation ->  resultString.append("%s; Invalid value: %s\n"
                        .formatted(violation.getMessage(), violation.getInvalidValue())));
        return new MessageDto(resultString.toString());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageDto handleMissingParameterException() {
        return new MessageDto("One or more request parameters are missing");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageDto handleValidationException(MethodArgumentNotValidException ex) {
        StringBuilder resultString = new StringBuilder();
        ex.getBindingResult().getFieldErrors()
                .forEach(fieldError ->  resultString.append("Field: %s; Error: %s\n"
                        .formatted(fieldError.getField(), fieldError.getDefaultMessage())));
        return new MessageDto(resultString.toString());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public MessageDto handleAuthException(AuthorizationDeniedException ex) {
        return new MessageDto("User is not authorized");
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public MessageDto handleUsernameAlreadyExist() {
        return new MessageDto("Username already exists");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageDto handleIllegalArgumentException(IllegalArgumentException ex) {
        return new MessageDto(ex.getMessage());
    }
}
