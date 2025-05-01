package com.sit.simpleissuetracker.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class IssueAssigneeUpdateRequest {
    @NotNull
    private Long assigneeId;
}