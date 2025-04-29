package com.sit.simpleissuetracker.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private Long ownerId; // Include owner ID, or a nested UserSummaryDto
    private String ownerName; // Example: Include owner name
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}