package com.sit.simpleissuetracker.dto;


import com.sit.simpleissuetracker.modals.IssuePriority;
import com.sit.simpleissuetracker.modals.IssueStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IssueResponse {
    private Long id;
    private String title;
    private String description;
    private IssueStatus status;
    private IssuePriority priority;
    private Long projectId;
    private String projectName; // Example derived field
    private UserSummaryDto reporter; // Use a simplified User DTO
    private UserSummaryDto assignee; // Use a simplified User DTO (can be null)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int commentCount; // Example derived field
}
