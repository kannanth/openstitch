package com.openstitch.api.exception;

import com.openstitch.api.dto.ErrorResponse;
import com.openstitch.engine.exception.DataParseException;
import com.openstitch.engine.exception.RenderException;
import com.openstitch.engine.exception.StorageException;
import com.openstitch.engine.exception.TemplateParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TemplateParseException.class)
    public ResponseEntity<ErrorResponse> handleTemplateParse(TemplateParseException ex) {
        log.warn("Template parse error: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "Bad Request", "Invalid template: " + ex.getMessage()));
    }

    @ExceptionHandler(DataParseException.class)
    public ResponseEntity<ErrorResponse> handleDataParse(DataParseException ex) {
        log.warn("Data parse error: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "Bad Request", "Invalid data: " + ex.getMessage()));
    }

    @ExceptionHandler(RenderException.class)
    public ResponseEntity<ErrorResponse> handleRender(RenderException ex) {
        log.error("PDF rendering failed: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal Server Error", "PDF rendering failed: " + ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorage(StorageException ex) {
        log.error("Storage error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal Server Error", "Storage error: " + ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        log.warn("Validation error: {}", message);
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "Bad Request", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal Server Error", ex.getMessage()));
    }
}
