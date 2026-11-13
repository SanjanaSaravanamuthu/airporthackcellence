package com.airporttaxi.exception;

import com.airporttaxi.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleRuleViolationException(RuleViolationException ex) {
        log.warn("Rule violation encountered [{}]: {}", ex.getRule(), ex.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String messageLower = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (messageLower.contains("not found") || "NOT_FOUND".equalsIgnoreCase(ex.getRule())) {
            status = HttpStatus.NOT_FOUND;
        }

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .rule(ex.getRule())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("Input validation failed: {}", details);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .rule("INPUT_VALIDATION")
                .message(details)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .rule("INTERNAL_ERROR")
                .message(ex.getMessage() != null ? ex.getMessage() : "An unexpected internal server error occurred")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
