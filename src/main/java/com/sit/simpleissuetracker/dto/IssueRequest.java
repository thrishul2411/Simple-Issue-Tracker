package com.sit.simpleissuetracker.dto;

import com.sit.simpleissuetracker.modals.IssuePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IssueRequest {
    @NotBlank
    @Size(min = 5, max = 200)
    private String title;

    @Size(max = 10000)
    private String description;

    // projectId is usually taken from the URL path variable
    // reporterId is usually the logged-in user (set in service)

    private Long assigneeId; // Optional: ID of user to assign initially
    private IssuePriority priority; // Optional
}
