package com.sit.simpleissuetracker.controller;
import com.sit.simpleissuetracker.dto.UserResponse;
import com.sit.simpleissuetracker.dto.UserSummaryDto;
import com.sit.simpleissuetracker.modals.User;
import com.sit.simpleissuetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    // Simple mapper method (Ideally use MapStruct or move to UserService/Mapper class)
    private UserResponse mapUserToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        userResponse.setRoles(roleNames);
        userResponse.setCreatedAt(user.getCreatedAt());
        return userResponse;
    }

    // Simple mapper method (Ideally use MapStruct or move to UserService/Mapper class)
    private UserSummaryDto mapUserToSummaryDto(User user) {
        if (user == null) return null;
        return new UserSummaryDto(
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail()
        );
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Attempt to access /me endpoint without authentication.");
            // Although Spring Security should block this, adding an explicit check
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        String userEmail = authentication.getName(); // Use getName() which returns the principal name (our email)
        log.debug("Fetching details for current user: {}", userEmail);

        try {
            User currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Authenticated user '" + userEmail + "' not found in database."));

            return ResponseEntity.ok(mapUserToUserResponse(currentUser));
        } catch (UsernameNotFoundException e) {
            log.error("Data inconsistency: Authenticated user {} not found.", userEmail, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping
    public ResponseEntity<List<UserSummaryDto>> getAllUsersForAssigneeList() {

        log.debug("Fetching list of users for assignment.");
        List<User> users = userRepository.findAll(); // Fetch all users
        List<UserSummaryDto> userSummaries = users.stream()
                .map(this::mapUserToSummaryDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userSummaries);
    }
}