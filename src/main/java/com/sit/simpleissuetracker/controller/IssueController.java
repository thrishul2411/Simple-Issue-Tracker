package com.sit.simpleissuetracker.controller;

import com.sit.simpleissuetracker.dto.IssueAssigneeUpdateRequest;
import com.sit.simpleissuetracker.dto.IssueRequest;
import com.sit.simpleissuetracker.dto.IssueResponse;
import com.sit.simpleissuetracker.dto.IssueStatusUpdateRequest;
import com.sit.simpleissuetracker.exception.BadRequestException;
import com.sit.simpleissuetracker.exception.ResourceNotFoundException;
import com.sit.simpleissuetracker.service.IssueService;
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
@RequestMapping("/api/v1") // Base path, specific paths defined in methods
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;
    private static final Logger log = LoggerFactory.getLogger(IssueController.class);

    @PostMapping("/projects/{projectId}/issues")
    public ResponseEntity<IssueResponse> createIssue(@PathVariable Long projectId,
                                                     @Valid @RequestBody IssueRequest issueRequest) {
        log.info("Received request to create issue in project ID {}: {}", projectId, issueRequest.getTitle());
        try {
            IssueResponse createdIssue = issueService.createIssue(projectId, issueRequest);
            return new ResponseEntity<>(createdIssue, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            // Project or Assignee not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error creating issue in project {}: {}", projectId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred creating the issue.", e);
        }
    }

    @GetMapping("/projects/{projectId}/issues")
    public ResponseEntity<List<IssueResponse>> getIssuesByProjectId(@PathVariable Long projectId) {
        log.debug("Received request to get issues for project ID: {}", projectId);
        try {
            List<IssueResponse> issues = issueService.getIssuesByProjectId(projectId);
            return ResponseEntity.ok(issues);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/issues/{issueId}")
    public ResponseEntity<IssueResponse> getIssueById(@PathVariable Long issueId) {
        log.debug("Received request to get issue by ID: {}", issueId);
        try {
            IssueResponse issue = issueService.getIssueById(issueId);
            return ResponseEntity.ok(issue);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    // Using PUT for full updates of core details (title, desc, priority) based on IssueRequest DTO
    @PutMapping("/issues/{issueId}")
    public ResponseEntity<IssueResponse> updateIssueDetails(@PathVariable Long issueId,
                                                            @Valid @RequestBody IssueRequest issueRequest) {
        log.info("Received request to update issue details for ID: {}", issueId);
        try {
            IssueResponse updatedIssue = issueService.updateIssueDetails(issueId, issueRequest);
            return ResponseEntity.ok(updatedIssue);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error updating issue details {}: {}", issueId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred updating issue details.", e);
        }
    }

    @PatchMapping("/issues/{issueId}/status")
    public ResponseEntity<IssueResponse> updateIssueStatus(@PathVariable Long issueId,
                                                           @Valid @RequestBody IssueStatusUpdateRequest statusUpdateRequest) {
        log.info("Received request to update status for issue ID: {}", issueId);
        try {
            IssueResponse updatedIssue = issueService.updateIssueStatus(issueId, statusUpdateRequest);
            return ResponseEntity.ok(updatedIssue);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error updating issue status {}: {}", issueId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred updating issue status.", e);
        }
    }

    @PatchMapping("/issues/{issueId}/assignee")
    public ResponseEntity<IssueResponse> updateIssueAssignee(@PathVariable Long issueId,
                                                             @Valid @RequestBody IssueAssigneeUpdateRequest assigneeUpdateRequest) {
        log.info("Received request to update assignee for issue ID: {}", issueId);
        try {
            IssueResponse updatedIssue = issueService.updateIssueAssignee(issueId, assigneeUpdateRequest);
            return ResponseEntity.ok(updatedIssue);
        } catch (ResourceNotFoundException e) {
            // Issue or Assignee not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error updating issue assignee {}: {}", issueId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred updating issue assignee.", e);
        }
    }

    @DeleteMapping("/issues/{issueId}")
    public ResponseEntity<Void> deleteIssue(@PathVariable Long issueId) {
        log.warn("Received request to DELETE issue ID: {}", issueId);
        try {
            issueService.deleteIssue(issueId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error deleting issue {}: {}", issueId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred deleting the issue.", e);
        }
    }
}
