package com.sit.simpleissuetracker.controller;

import com.sit.simpleissuetracker.dto.SignUpRequest;
import com.sit.simpleissuetracker.dto.UserResponse;
import com.sit.simpleissuetracker.exception.BadRequestException;
import com.sit.simpleissuetracker.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException; // To handle exceptions simply for now

@RestController
@RequestMapping("/api/v1/auth") // Base path for authentication endpoints
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        log.info("Received registration request for email: {}", signUpRequest.getEmail());
        try {
            UserResponse registeredUser = authService.registerUser(signUpRequest);
            // Return 201 Created status with the created user details
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (BadRequestException e) {
            // Handle specific known exceptions from the service
            log.warn("Registration failed: {}", e.getMessage());
            // Throw ResponseStatusException to let Spring handle the HTTP status code
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            // Catch unexpected errors
            log.error("Unexpected error during registration for email {}: {}", signUpRequest.getEmail(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during registration.", e);
        }
    }

}