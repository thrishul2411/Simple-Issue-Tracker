package com.sit.simpleissuetracker.service.impl;

import com.sit.simpleissuetracker.dto.*;
import com.sit.simpleissuetracker.exception.BadRequestException;
import com.sit.simpleissuetracker.exception.ResourceNotFoundException;
import com.sit.simpleissuetracker.modals.*;
import com.sit.simpleissuetracker.repository.CommentRepository;
import com.sit.simpleissuetracker.repository.IssueRepository;
import com.sit.simpleissuetracker.repository.ProjectRepository;
import com.sit.simpleissuetracker.repository.UserRepository;
import com.sit.simpleissuetracker.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository; // Needed for comment count in mapping

    private static final Logger log = LoggerFactory.getLogger(IssueServiceImpl.class);

    @Override
    @Transactional
    public IssueResponse createIssue(Long projectId, IssueRequest issueRequest) throws ResourceNotFoundException {
        User currentUser = getCurrentUser();
        log.info("User '{}' creating issue in project ID {} with title '{}'",
                currentUser.getEmail(), projectId, issueRequest.getTitle());

        // 1. Find the project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        // 2. Find assignee if provided
        User assignee = null;
        if (issueRequest.getAssigneeId() != null) {
            assignee = userRepository.findById(issueRequest.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee user not found with ID: " + issueRequest.getAssigneeId()));
            log.debug("Assignee found: {}", assignee.getEmail());
        }

        // 3. Create new Issue entity
        Issue issue = new Issue();
        issue.setTitle(issueRequest.getTitle());
        issue.setDescription(issueRequest.getDescription());
        issue.setProject(project);
        issue.setReporter(currentUser); // Logged-in user is the reporter
        issue.setAssignee(assignee); // Can be null
        issue.setStatus(IssueStatus.OPEN); // Default status
        issue.setPriority(issueRequest.getPriority() != null ? issueRequest.getPriority() : IssuePriority.MEDIUM); // Default priority

        Issue savedIssue = issueRepository.save(issue);
        log.info("Issue created successfully with ID {} in project {}", savedIssue.getId(), projectId);

        return mapIssueToResponse(savedIssue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponse> getIssuesByProjectId(Long projectId) throws ResourceNotFoundException {
        log.debug("Fetching issues for project ID: {}", projectId);
        // Check if project exists first
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with ID: " + projectId);
        }
        // Fetch issues using the repository method
        List<Issue> issues = issueRepository.findByProjectId(projectId);
        return issues.stream()
                .map(this::mapIssueToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public IssueResponse getIssueById(Long issueId) throws ResourceNotFoundException {
        log.debug("Fetching issue with ID: {}", issueId);
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));
        return mapIssueToResponse(issue);
    }

    @Override
    @Transactional
    public IssueResponse updateIssueDetails(Long issueId, IssueRequest issueRequest) throws ResourceNotFoundException {
        User currentUser = getCurrentUser();
        log.info("User '{}' attempting to update details for issue ID: {}", currentUser.getEmail(), issueId);

        Issue existingIssue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        // --- Authorization Check
        checkPermissionForIssue(currentUser, existingIssue, "update details");
        // --- End Authorization Check ---

        existingIssue.setTitle(issueRequest.getTitle());
        existingIssue.setDescription(issueRequest.getDescription());
        if (issueRequest.getPriority() != null) { // Allow updating priority here
            existingIssue.setPriority(issueRequest.getPriority());
        }

        Issue updatedIssue = issueRepository.save(existingIssue);
        log.info("Issue details updated successfully for ID: {}", updatedIssue.getId());
        return mapIssueToResponse(updatedIssue);
    }

    @Override
    @Transactional
    public IssueResponse updateIssueStatus(Long issueId, IssueStatusUpdateRequest statusUpdateRequest) throws ResourceNotFoundException, BadRequestException {
        User currentUser = getCurrentUser();
        log.info("User '{}' attempting to update status for issue ID: {} to {}",
                currentUser.getEmail(), issueId, statusUpdateRequest.getStatus());

        Issue existingIssue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        // --- Authorization Check
        checkPermissionForIssue(currentUser, existingIssue, "update status");
        // --- End Authorization Check ---


        // Optional: Add status transition validation logic here if needed
        // e.g., cannot move directly from OPEN to RESOLVED, etc.
        IssueStatus newStatus = statusUpdateRequest.getStatus();
        if (newStatus == null) {
            throw new BadRequestException("New status cannot be null.");
        }
        // Example validation: if (!isValidTransition(existingIssue.getStatus(), newStatus)) { ... }

        existingIssue.setStatus(newStatus);

        Issue updatedIssue = issueRepository.save(existingIssue);
        log.info("Issue status updated successfully for ID: {}", updatedIssue.getId());
        return mapIssueToResponse(updatedIssue);
    }

    @Override
    @Transactional
    public IssueResponse updateIssueAssignee(Long issueId, IssueAssigneeUpdateRequest assigneeUpdateRequest) throws ResourceNotFoundException {
        User currentUser = getCurrentUser();
        log.info("User '{}' attempting to update assignee for issue ID: {} to user ID {}",
                currentUser.getEmail(), issueId, assigneeUpdateRequest.getAssigneeId());

        Issue existingIssue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        // --- Authorization Check Example ---
        checkPermissionForIssue(currentUser, existingIssue, "update assignee");
        // --- End Authorization Check ---


        User newAssignee = null;
        if (assigneeUpdateRequest.getAssigneeId() != null) {
            newAssignee = userRepository.findById(assigneeUpdateRequest.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee user not found with ID: " + assigneeUpdateRequest.getAssigneeId()));
            log.debug("New assignee found: {}", newAssignee.getEmail());
        } else {
            log.info("Unassigning issue ID: {}", issueId);
        }

        existingIssue.setAssignee(newAssignee); // Set to new user or null

        Issue updatedIssue = issueRepository.save(existingIssue);
        log.info("Issue assignee updated successfully for ID: {}", updatedIssue.getId());
        return mapIssueToResponse(updatedIssue);
    }


    @Override
    @Transactional
    public void deleteIssue(Long issueId) throws ResourceNotFoundException {
        User currentUser = getCurrentUser();
        log.warn("User '{}' attempting to DELETE issue with ID: {}", currentUser.getEmail(), issueId);

        Issue issueToDelete = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        // --- Authorization Check Example ---
        // Only Admins can delete? Or Reporter? Define the rule.
        if (!isUserAdmin(currentUser) /* && !issueToDelete.getReporter().getId().equals(currentUser.getId()) */ ) {
            log.warn("Authorization failed: User '{}' cannot delete issue ID {}", currentUser.getEmail(), issueId);
            throw new AccessDeniedException("User does not have permission to delete this issue");
        }
        // --- End Authorization Check ---


        // Deleting an issue might automatically delete comments if CascadeType.REMOVE/ALL is set
        // on Issue entity's comments field. Otherwise, comments would remain orphaned.
        issueRepository.delete(issueToDelete);
        log.warn("Issue with ID {} deleted successfully by user '{}'.", issueId, currentUser.getEmail());
    }


    // ========================================================================
    // Helper Methods
    // ========================================================================

    private User getCurrentUser() {
        // (Same implementation as in ProjectServiceImpl - consider moving to a common utility/service)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("No authenticated user found in security context.");
        }
        String userEmail = (String) authentication.getPrincipal();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user '" + userEmail + "' not found in database. Data inconsistency?"));
    }

    private boolean isUserAdmin(User user) {
        // (Same implementation as in ProjectServiceImpl - consider moving to a common utility/service)
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(RoleName.ROLE_ADMIN));
    }

    // --- NEW Helper: Basic Permission Check for Issues ---
    private void checkPermissionForIssue(User user, Issue issue, String action) {
        boolean isAdmin = isUserAdmin(user);
        boolean isReporter = issue.getReporter().getId().equals(user.getId());
        boolean isAssignee = issue.getAssignee() != null && issue.getAssignee().getId().equals(user.getId());

        // Example Policy: Admin, Reporter, or Assignee can modify
        if (!isAdmin && !isReporter && !isAssignee) {
            log.warn("Authorization failed: User '{}' cannot {} issue ID {}", user.getEmail(), action, issue.getId());
            throw new AccessDeniedException("User does not have permission to " + action + " on this issue");
        }
        // Adjust this logic based on your specific permission requirements for each action
    }
    // --- End NEW Helper ---


    // --- Helper: Mapper (Consider MapStruct) ---
    private IssueResponse mapIssueToResponse(Issue issue) {
        IssueResponse response = new IssueResponse();
        response.setId(issue.getId());
        response.setTitle(issue.getTitle());
        response.setDescription(issue.getDescription());
        response.setStatus(issue.getStatus());
        response.setPriority(issue.getPriority());
        response.setCreatedAt(issue.getCreatedAt());
        response.setUpdatedAt(issue.getUpdatedAt());

        // Map Project Info
        if (issue.getProject() != null) {
            response.setProjectId(issue.getProject().getId());
            response.setProjectName(issue.getProject().getName()); // Include project name
        }

        // Map Reporter Info (Use UserSummaryDto)
        if (issue.getReporter() != null) {
            response.setReporter(mapUserToSummaryDto(issue.getReporter()));
        }

        // Map Assignee Info (Use UserSummaryDto, can be null)
        if (issue.getAssignee() != null) {
            response.setAssignee(mapUserToSummaryDto(issue.getAssignee()));
        } else {
            response.setAssignee(null);
        }

        // Add comment count (requires CommentRepository)
        long commentCount = commentRepository.countByIssueId(issue.getId()); // Need countByIssueId in CommentRepository
        response.setCommentCount((int) commentCount); // Cast long to int for DTO

        return response;
    }

    private UserSummaryDto mapUserToSummaryDto(User user) {
        if (user == null) return null;
        return new UserSummaryDto(
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail()
        );
    }

}
