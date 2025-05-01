package com.sit.simpleissuetracker.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ErrorDetails {
    private LocalDateTime timestamp;
    private int status;
    private String error; // General error category (e.g., "Not Found", "Bad Request")
    private String message; // Specific error message
    private String path; // The URL path where the error occurred

    // Optional: For validation errors
    private Map<String, List<String>> validationErrors; // Field -> List of error messages

    public ErrorDetails(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // Constructor including validation errors
    public ErrorDetails(LocalDateTime timestamp, int status, String error, String message, String path, Map<String, List<String>> validationErrors) {
        this(timestamp, status, error, message, path);
        this.validationErrors = validationErrors;
    }
}
