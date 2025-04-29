package com.sit.simpleissuetracker.dto;

import lombok.Data;
// Consider adding @NotNull if assigning null shouldn't be done via this endpoint
import jakarta.validation.constraints.NotNull;

@Data
public class IssueAssigneeUpdateRequest {
    @NotNull // If assigning to null (unassigning) requires a different action or validation
    private Long assigneeId; // ID of the user to assign to (can be null to unassign?) - clarify requirement
}