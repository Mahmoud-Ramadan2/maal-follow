package com.mahmoud.maalflow.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;

@RestControllerAdvice
@Slf4j
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

    // Handle MissingServletRequestParameterException 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        Map<String, String> error = new HashMap<>();
        String message = messageSource.getMessage("validation.parameter.missing", new Object[]{ex.getParameterName()}, LocaleContextHolder.getLocale());
        error.put("message", message);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(InvalidPropertiesFormatException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPropertiesFormatException(InvalidPropertiesFormatException ex) {
        Map<String, String> error = new HashMap<>();
        String message = messageSource.getMessage("validation.properties.invalidFormat", null, LocaleContextHolder.getLocale());
        error.put("message", message);
        return ResponseEntity.badRequest().body(error);
    }

    // Handle ThrowGeneralException 400
    @ExceptionHandler(ThrowGeneralException.class)
    public ResponseEntity<String> handleThrowGeneralException(ThrowGeneralException ex) {
        String message = messageSource.getMessage(ex.getMessageKey(), null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    // Handle BusinessException 400
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(BusinessException ex) {
        String message = messageSource.getMessage(ex.getMessageKey(), null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    // Handle DataIntegrityViolationException 400
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = messageSource.getMessage("validation.data.integrityViolation", null, LocaleContextHolder.getLocale());
        log.error("Data Integrity Violation: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    // Handle SQLIntegrityConstraintViolationException 400
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<String> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        String message = messageSource.getMessage("validation.sql.integrityConstraintViolation", null, LocaleContextHolder.getLocale());
        log.error("SQL Integrity Constraint Violation: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    // Handle HttpMessageNotWritableException 500
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<String> handleHttpMessageNotWritableException(HttpMessageNotWritableException ex) {
        String message = messageSource.getMessage("validation.json.writeError", null, LocaleContextHolder.getLocale());
        log.error("HTTP Message Not Writable: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }

    // Handle MethodArgumentTypeMismatchException 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = messageSource.getMessage("validation.argument.typeMismatch", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

//   Handle NoResourceFoundException 404
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException ex) {
        String message = messageSource.getMessage("validation.resource.notFound", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    //
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        String message = messageSource.getMessage("validation.method.notSupported", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(message);
    }
}
