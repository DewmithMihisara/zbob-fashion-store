package com.zbob.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> notFound(ResourceNotFoundException e) {
        return respond(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiErrorResponse> conflict(ConflictException e) {
        return respond(HttpStatus.CONFLICT, "CONFLICT", e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ApiErrorResponse> badRequest(BadRequestException e) {
        return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", e.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiErrorResponse> invalidCredentials(InvalidCredentialsException e) {
        return respond(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiErrorResponse> tooLarge(MaxUploadSizeExceededException e) {
        return respond(HttpStatus.CONTENT_TOO_LARGE, "FILE_TOO_LARGE", "Images can be at most 10 MB");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> unreadable(HttpMessageNotReadableException e) {
        return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Malformed request body");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> invalid(MethodArgumentNotValidException e) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(400, "VALIDATION_FAILED", "Request validation failed", fields));
    }

    /** Rethrown so the security filter chain produces the 401 and 403 responses. */
    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    void securityPassthrough(RuntimeException e) {
        throw e;
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> unexpected(Exception e) throws Exception {
        if (e instanceof ErrorResponse) {
            throw e; // framework 4xx such as 404 and 405 keep their own status
        }
        log.error("Unhandled exception", e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Something went wrong");
    }

    private ResponseEntity<ApiErrorResponse> respond(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), code, message));
    }
}
