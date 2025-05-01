package com.sit.simpleissuetracker.service.impl;

import com.sit.simpleissuetracker.dto.SignUpRequest;
import com.sit.simpleissuetracker.dto.UserResponse;
import com.sit.simpleissuetracker.exception.BadRequestException;
import com.sit.simpleissuetracker.exception.ResourceNotFoundException;
import com.sit.simpleissuetracker.modals.Role;
import com.sit.simpleissuetracker.modals.RoleName;
import com.sit.simpleissuetracker.modals.User;
import com.sit.simpleissuetracker.repository.RoleRepository;
import com.sit.simpleissuetracker.repository.UserRepository;
import com.sit.simpleissuetracker.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class.getName());

    @Override
    @Transactional
    public UserResponse registerUser(SignUpRequest signUpRequest) throws BadRequestException, ResourceNotFoundException {
        log.info("Attempting to register user with email: " + signUpRequest.getEmail());

        // 1. Check if email already exists
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            log.warn("Registration failed: Email already exists." + signUpRequest.getEmail());
            throw new BadRequestException("Email address already in use: " + signUpRequest.getEmail());
        }

        // 2. Create new user object
        User user = new User();
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setEmail(signUpRequest.getEmail());

        // 3. Encode password
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        // 4. Assign default role (ROLE_USER)
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> {
                    // This should ideally not happen if DataLoader ran correctly
                    log.error("CRITICAL: ROLE_USER not found in database!");
                    return new ResourceNotFoundException("Default user role not found.");
                });
        user.setRoles(Set.of(userRole));

        // 5. Save user to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with email: " +  savedUser.getEmail());

        // 6. Map to DTO and return
        // This mapping logic could be moved to a dedicated Mapper class later
        return mapUserToUserResponse(savedUser);
    }

    // --- Helper Mapper Method (Consider MapStruct later) ---
    private UserResponse mapUserToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        // Map Role entities to a Set of String role names
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name()) // Get enum name as String
                .collect(Collectors.toSet());
        userResponse.setRoles(roleNames);
        userResponse.setCreatedAt(user.getCreatedAt()); // Assuming User entity has @CreationTimestamp
        return userResponse;
    }
}
