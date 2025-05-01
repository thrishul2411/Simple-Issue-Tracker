package com.sit.simpleissuetracker.service;

import com.sit.simpleissuetracker.dto.IssueAssigneeUpdateRequest;
import com.sit.simpleissuetracker.dto.IssueRequest;
import com.sit.simpleissuetracker.dto.IssueResponse;
import com.sit.simpleissuetracker.dto.IssueStatusUpdateRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import com.sit.simpleissuetracker.exception.ResourceNotFoundException;
import com.sit.simpleissuetracker.exception.BadRequestException;
import java.util.List;

public interface IssueService {
    /**
     * Creates a new issue within a specific project.
     * The reporter is set to the currently authenticated user.
     * @param projectId The ID of the project to add the issue to.
     * @param issueRequest DTO containing issue details (title, description, assigneeId, priority).
     * @return DTO representing the newly created issue.
     * @throws ConfigDataResourceNotFoundException if the project or optional assignee user is not found.
     */
    IssueResponse createIssue(Long projectId, IssueRequest issueRequest) throws ResourceNotFoundException;

    /**
     * Retrieves a list of issues for a specific project.
     * (Future enhancement: Add filtering by status, assignee, priority etc.)
     * @param projectId The ID of the project whose issues are to be retrieved.
     * @return A list of issue DTOs.
     * @throws ResourceNotFoundException if the project with the given ID is not found.
     */
    List<IssueResponse> getIssuesByProjectId(Long projectId) throws ResourceNotFoundException;

    /**
     * Retrieves a specific issue by its ID.
     * @param issueId The ID of the issue to retrieve.
     * @return DTO representing the issue.
     * @throws ResourceNotFoundException if the issue with the given ID is not found.
     */
    IssueResponse getIssueById(Long issueId) throws ResourceNotFoundException;

    /**
     * Updates the core details (title, description, priority) of an existing issue.
     * Requires appropriate permissions (e.g., reporter, assignee, admin).
     * @param issueId The ID of the issue to update.
     * @param issueRequest DTO containing updated details.
     * @return DTO representing the updated issue.
     * @throws ResourceNotFoundException if the issue is not found.
     * @throws AccessDeniedException if the user lacks permission.
     */
    IssueResponse updateIssueDetails(Long issueId, IssueRequest issueRequest) throws ResourceNotFoundException;

    /**
     * Updates the status of an existing issue.
     * Requires appropriate permissions. Validates status transition if needed.
     * @param issueId The ID of the issue to update.
     * @param statusUpdateRequest DTO containing the new status.
     * @return DTO representing the updated issue.
     * @throws ResourceNotFoundException if the issue is not found.
     * @throws BadRequestException if the status transition is invalid.
     * @throws AccessDeniedException if the user lacks permission.
     */
    IssueResponse updateIssueStatus(Long issueId, IssueStatusUpdateRequest statusUpdateRequest) throws ResourceNotFoundException, BadRequestException;

    /**
     * Updates the assignee of an existing issue.
     * Requires appropriate permissions. Verifies the assignee user exists.
     * @param issueId The ID of the issue to update.
     * @param assigneeUpdateRequest DTO containing the new assignee's ID (can be null to unassign).
     * @return DTO representing the updated issue.
     * @throws ResourceNotFoundException if the issue or the new assignee user (if not null) is not found.
     * @throws AccessDeniedException if the user lacks permission.
     */
    IssueResponse updateIssueAssignee(Long issueId, IssueAssigneeUpdateRequest assigneeUpdateRequest) throws ResourceNotFoundException;

    /**
     * Deletes an issue by its ID.
     * Requires appropriate permissions (e.g., admin, potentially reporter).
     * @param issueId The ID of the issue to delete.
     * @throws ResourceNotFoundException if the issue with the given ID is not found.
     * @throws AccessDeniedException if the user lacks permission.
     */
    void deleteIssue(Long issueId) throws ResourceNotFoundException;
}
