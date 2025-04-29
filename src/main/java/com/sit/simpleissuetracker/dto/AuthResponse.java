package com.sit.simpleissuetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    // For session-based auth, might not need much here initially
    // Could return basic user info or a success message
    private String message;
    private UserResponse user; // Or just userId/email
    // String accessToken; // Add this later if using JWT
}
