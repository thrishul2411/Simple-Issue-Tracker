package com.sit.simpleissuetracker.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private String body;
    private UserSummaryDto author;
    private Long issueId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}