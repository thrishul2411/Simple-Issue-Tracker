package com.sit.simpleissuetracker.service;

import com.sit.simpleissuetracker.dto.CommentRequest;
import com.sit.simpleissuetracker.dto.CommentResponse;
import org.springframework.security.access.AccessDeniedException;
import com.sit.simpleissuetracker.exception.ResourceNotFoundException;

import java.util.List;

public interface CommentService {

    /**
     * Creates a new comment on a specific issue.
     * The author is set to the currently authenticated user.
     * @param issueId The ID of the issue to add the comment to.
     * @param commentRequest DTO containing the comment body.
     * @return DTO representing the newly created comment.
     * @throws ResourceNotFoundException if the issue with the given ID is not found.
     */
    CommentResponse createComment(Long issueId, CommentRequest commentRequest) throws ResourceNotFoundException;

    /**
     * Retrieves a list of comments for a specific issue, ordered by creation date.
     * @param issueId The ID of the issue whose comments are to be retrieved.
     * @return A list of comment DTOs.
     * @throws ResourceNotFoundException if the issue with the given ID is not found.
     */
    List<CommentResponse> getCommentsByIssueId(Long issueId) throws ResourceNotFoundException;

    /**
     * Deletes a comment by its ID.
     * Requires appropriate permissions (e.g., comment author, admin).
     * @param commentId The ID of the comment to delete.
     * @throws ResourceNotFoundException if the comment with the given ID is not found.
     * @throws AccessDeniedException if the user lacks permission.
     */
    void deleteComment(Long commentId) throws ResourceNotFoundException; // Added optional delete endpoint consideration

}
