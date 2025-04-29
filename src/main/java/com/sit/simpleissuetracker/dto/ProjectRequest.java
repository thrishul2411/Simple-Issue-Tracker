package com.sit.simpleissuetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectRequest {
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @Size(max = 5000) // Example size limit for description
    private String description;

    // ownerId might be set automatically based on logged-in user in service layer
}