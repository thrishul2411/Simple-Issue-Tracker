package com.sit.simpleissuetracker.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Set<String> roles; // Return role names as strings
    private LocalDateTime createdAt;
}
