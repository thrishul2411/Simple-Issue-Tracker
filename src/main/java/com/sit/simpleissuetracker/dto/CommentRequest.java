package com.sit.simpleissuetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank
    @Size(min = 1, max = 5000)
    private String body;
    // issueId is from path variable
    // authorId is logged-in user (set in service)
}
