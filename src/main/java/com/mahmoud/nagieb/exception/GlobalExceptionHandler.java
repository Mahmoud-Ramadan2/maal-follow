package com.mahmoud.nagieb.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    //  Handle MethodArgumentNotValidException 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
                fieldError -> errors.put(fieldError.getField(), fieldError.getDefaultMessage())
                );

        return ResponseEntity.badRequest().body(errors);
    }

    //  Handle UserNotFoundException 404
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getParameters(), LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }
    //  Handle DuplicateNationalIdException 400
    @ExceptionHandler(DuplicateNationalIdException.class)
    public ResponseEntity<String> handleDuplicateNationalIdException(DuplicateNationalIdException ex) {
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getParameters(), LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    //  Handle AccessDeniedException 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        String message = messageSource.getMessage(ex.getMessageKey(), null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
    }

    //  Handle DuplicateVendorNameException 400
    @ExceptionHandler(DuplicateVendorNameException.class)
    public ResponseEntity<String> handleDuplicateVendorNameException(DuplicateVendorNameException ex) {
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getParameters(), LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    //  Handle DuplicateVendorPhoneException 400
    @ExceptionHandler(DuplicateVendorPhoneException.class)
    public ResponseEntity<String> handleDuplicateVendorPhoneException(DuplicateVendorPhoneException ex) {
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getParameters(), LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    //  Handle GeneralNotFoundException 404
    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<String> handleVendorNotFoundException(ObjectNotFoundException ex) {
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getParameters(), LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    // Handle HttpMessageNotReadableException 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, String> error = new HashMap<>();
        String message = messageSource.getMessage("validation.json.invalid", null, LocaleContextHolder.getLocale());
        error.put("message", message);
        return ResponseEntity.badRequest().body(error);
    }

}
