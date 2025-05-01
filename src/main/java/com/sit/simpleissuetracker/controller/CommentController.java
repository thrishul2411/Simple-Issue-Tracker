package com.sit.simpleissuetracker.controller;

import com.sit.simpleissuetracker.dto.CommentRequest;
import com.sit.simpleissuetracker.dto.CommentResponse;
import com.sit.simpleissuetracker.exception.ResourceNotFoundException;
import com.sit.simpleissuetracker.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/issues/{issueId}/comments") // Base path is nested under issues
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long issueId,
                                                         @Valid @RequestBody CommentRequest commentRequest) {
        log.info("Received request to add comment to issue ID: {}", issueId);
        try {
            CommentResponse createdComment = commentService.createComment(issueId, commentRequest);
            return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            // Issue not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error creating comment for issue {}: {}", issueId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred creating the comment.", e);
        }
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsByIssueId(@PathVariable Long issueId) {
        log.debug("Received request to get comments for issue ID: {}", issueId);
        try {
            List<CommentResponse> comments = commentService.getCommentsByIssueId(issueId);
            return ResponseEntity.ok(comments);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    // Optional: Delete comment endpoint
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long issueId, @PathVariable Long commentId) {
        log.warn("Received request to DELETE comment ID: {} from issue ID: {}", commentId, issueId);
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error deleting comment {}: {}", commentId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred deleting the comment.", e);
        }
    }
}
