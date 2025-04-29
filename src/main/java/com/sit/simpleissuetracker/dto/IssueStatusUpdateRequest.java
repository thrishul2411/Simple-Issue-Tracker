package com.sit.simpleissuetracker.dto;

import com.sit.simpleissuetracker.modals.IssueStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IssueStatusUpdateRequest {
    @NotNull // Ensure status is provided
    private IssueStatus status;
}