package com.sit.simpleissuetracker.service;

import com.sit.simpleissuetracker.dto.SignUpRequest;
import com.sit.simpleissuetracker.dto.UserResponse;
import com.sit.simpleissuetracker.exception.BadRequestException;
import com.sit.simpleissuetracker.exception.ResourceNotFoundException;

public interface AuthService {
    /**
     * Registers a new user in the system.
     * @param signUpRequest DTO containing registration details.
     * @return DTO representing the newly created user.
     * @throws BadRequestException if email already exists.
     */
    UserResponse registerUser(SignUpRequest signUpRequest) throws BadRequestException, ResourceNotFoundException;
}
