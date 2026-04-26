package com.mahmoud.maalflow.exception;

import com.mahmoud.maalflow.exception.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
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
import java.time.LocalDateTime;
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
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = messageSource.getMessage("validation.entered.values",null,  LocaleContextHolder.getLocale());
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
                fieldError -> fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    //  Handle UserNotFoundException 404
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request) {
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getParameters(), LocaleContextHolder.getLocale());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    //  Handle DuplicateNationalIdException 400
    @ExceptionHandler(DuplicateNationalIdException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateNationalIdException(
            DuplicateNationalIdException ex, HttpServletRequest request) {
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getParameters(), LocaleContextHolder.getLocale());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    //  Handle AccessDeniedException 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        String message = messageSource.getMessage(ex.getMessageKey(), null, LocaleContextHolder.getLocale());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    //  Handle DuplicateVendorNameException 400
    @ExceptionHandler(DuplicateVendorNameException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateVendorNameException(
            DuplicateVendorNameException ex, HttpServletRequest request) {
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getParameters(), LocaleContextHolder.getLocale());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    //  Handle DuplicateVendorPhoneException 400
    @ExceptionHandler(DuplicateVendorPhoneException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateVendorPhoneException(
            DuplicateVendorPhoneException ex, HttpServletRequest request) {
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getParameters(), LocaleContextHolder.getLocale());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    //  Handle GeneralNotFoundException 404
    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleVendorNotFoundException(
            ObjectNotFoundException ex, HttpServletRequest request) {
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getParameters(), LocaleContextHolder.getLocale());
        ApiErrorResponse response  = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // Handle HttpMessageNotReadableException 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

      String message = messageSource.getMessage("validation.json.invalid", null, LocaleContextHolder.getLocale());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        log.error("HTTP Message Not Readable: {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Handle MissingServletRequestParameterException 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = messageSource.getMessage("validation.parameter.missing", new Object[]{ex.getParameterName()}, LocaleContextHolder.getLocale());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        log.error("Missing request parameter: {}", request.getRequestURI(), ex);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(InvalidPropertiesFormatException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPropertiesFormatException(
            InvalidPropertiesFormatException ex, HttpServletRequest request) {
        Map<String, String> error = new HashMap<>();
        String message = messageSource.getMessage("validation.properties.invalidFormat", null, LocaleContextHolder.getLocale());
        error.put("message", message);
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        log.error("Invalid properties format: {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Handle ThrowGeneralException 400
    @ExceptionHandler(ThrowGeneralException.class)
    public ResponseEntity<ApiErrorResponse> handleThrowGeneralException(ThrowGeneralException ex
    , HttpServletRequest request) {
        String message = messageSource.getMessage(ex.getMessageKey(), null, LocaleContextHolder.getLocale());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        log.error("General exception: {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Handle BusinessException 400
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        String message = messageSource.getMessage(ex.getMessageKey(), null, LocaleContextHolder.getLocale());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        log.error("Business exception: {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Handle DataIntegrityViolationException 400
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,   HttpServletRequest request) {
        String message = messageSource.getMessage("validation.data.integrityViolation", null, LocaleContextHolder.getLocale());
        log.error("Data Integrity Violation: ", ex);
            ApiErrorResponse response = ApiErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(message)
                    .path(request.getRequestURI())
                    .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Handle SQLIntegrityConstraintViolationException 400
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleSQLIntegrityConstraintViolationException(
            SQLIntegrityConstraintViolationException ex, HttpServletRequest request) {
            String message = messageSource.getMessage("validation.sql.integrityConstraintViolation", null, LocaleContextHolder.getLocale());
            ApiErrorResponse response = ApiErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(message)
                    .path(request.getRequestURI())
                    .build();
        log.error("SQL Integrity Constraint Violation: {}", request.getRequestURI(), ex);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);


    }

    // Handle HttpMessageNotWritableException 500
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotWritableException(
            HttpMessageNotWritableException ex, HttpServletRequest request) {
        String message = messageSource.getMessage("validation.json.writeError", null, LocaleContextHolder.getLocale());
        log.error("HTTP Message Not Writable: ", ex);
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Handle MethodArgumentTypeMismatchException 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = messageSource.getMessage(
                "validation.argument.typeMismatch", new Object[]{ex.getName(), ex.getRequiredType().getSimpleName()}, LocaleContextHolder.getLocale());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        log.error("Method argument type mismatch: {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

//   Handle NoResourceFoundException 404

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {
        String message = messageSource.getMessage(
                "validation.resource.notFound", null, LocaleContextHolder.getLocale());

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        log.error("No resource found: {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

//    @ExceptionHandler(NoResourceFoundException.class)
//    public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException ex) {
//        String message = messageSource.getMessage("validation.resource.notFound", null, LocaleContextHolder.getLocale());
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
//    }

    //
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        String message = messageSource.getMessage("validation.method.notSupported", null, LocaleContextHolder .getLocale());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        log.error("HTTP method not supported: {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // Handle IllegalArgumentException 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException
    (IllegalArgumentException ex, HttpServletRequest request) {
        String message = messageSource.getMessage("validation.illegal.argument", null, LocaleContextHolder.getLocale());
        Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors .put("error", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();
        log.error("Illegal argument: {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    // Handle any other uncaught exceptions 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        String message = messageSource.getMessage("validation.internal.error", null, LocaleContextHolder.getLocale());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        log.error("Unexpected error: {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Handle Locking Failure (Conflict) 409

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handlePessimisticLockingFailure(PessimisticLockingFailureException ex, HttpServletRequest request) {
        String messageKey = "validation.capitalPool.concurrentUpdate";
        String message = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        log.error("Pessimistic lock timeout at {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    //  Handle ConstraintViolationException 400
    // for ex   . @Validated on method parameters, or when using Validator directly
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        String message = messageSource.getMessage("validation.constraint.violation", null, LocaleContextHolder.getLocale());
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            fieldErrors.put(fieldName, violation.getMessage());
        });

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        log.error("Constraint violation: {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
