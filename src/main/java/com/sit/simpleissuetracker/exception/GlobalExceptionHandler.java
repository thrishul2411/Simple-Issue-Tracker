package com.sit.simpleissuetracker.exception;

import com.sit.simpleissuetracker.dto.ErrorDetails;
import jakarta.servlet.http.HttpServletRequest; // Import HttpServletRequest
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest; // Import WebRequest

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice // Intercepts exceptions across all @Controllers
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle specific custom exceptions

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                ex.getMessage(),
                request.getDescription(false).substring(4) // Extract path from "uri="
        );
        log.warn("ResourceNotFoundException: {} on path {}", ex.getMessage(), errorDetails.getPath());
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDetails> handleBadRequestException(BadRequestException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).substring(4)
        );
        log.warn("BadRequestException: {} on path {}", ex.getMessage(), errorDetails.getPath());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class) // e.g., Cannot delete project with issues
    public ResponseEntity<ErrorDetails> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(), // 409 Conflict is appropriate here
                "Conflict",
                ex.getMessage(),
                request.getDescription(false).substring(4)
        );
        log.warn("IllegalStateException (Conflict): {} on path {}", ex.getMessage(), errorDetails.getPath());
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }


    // Handle Spring Security Access Denied Exception

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Access denied. You do not have permission to perform this action.", // Provide a generic message
                request.getDescription(false).substring(4)
        );
        log.warn("AccessDeniedException: User attempted unauthorized action on path {}", errorDetails.getPath()); // Avoid logging ex.getMessage() which might be null
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }


    // Handle Validation Errors from @Valid

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, List<String>> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
        });

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Input validation failed. Check 'validationErrors' for details.", // Generic message
                request.getDescription(false).substring(4),
                errors // Include the map of validation errors
        );
        log.warn("ValidationException: {} errors on path {}", errors.size(), errorDetails.getPath());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }


    // Handle Generic Exceptions (Catch-all)

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred.", // Avoid exposing internal details
                request.getDescription(false).substring(4)
        );
        // Log the full exception stack trace for internal debugging
        log.error("Unexpected Exception: {} on path {}", ex.getMessage(), errorDetails.getPath(), ex);
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
