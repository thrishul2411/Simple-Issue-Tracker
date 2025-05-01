package com.sit.simpleissuetracker.service.impl;

import com.sit.simpleissuetracker.dto.CommentRequest;
import com.sit.simpleissuetracker.dto.CommentResponse;
import com.sit.simpleissuetracker.dto.UserSummaryDto;
import com.sit.simpleissuetracker.exception.ResourceNotFoundException;
import com.sit.simpleissuetracker.modals.Comment;
import com.sit.simpleissuetracker.modals.Issue;
import com.sit.simpleissuetracker.modals.RoleName;
import com.sit.simpleissuetracker.modals.User;
import com.sit.simpleissuetracker.repository.CommentRepository;
import com.sit.simpleissuetracker.repository.IssueRepository;
import com.sit.simpleissuetracker.repository.UserRepository;
import com.sit.simpleissuetracker.service.CommentService;
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
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Override
    @Transactional
    public CommentResponse createComment(Long issueId, CommentRequest commentRequest) throws ResourceNotFoundException {
        User currentUser = getCurrentUser();
        log.info("User '{}' attempting to add comment to issue ID: {}", currentUser.getEmail(), issueId);

        // 1. Find the issue
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        // 2. Create new Comment entity
        Comment comment = new Comment();
        comment.setBody(commentRequest.getBody());
        comment.setIssue(issue);
        comment.setAuthor(currentUser); // Logged-in user is the author

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created successfully with ID {} on issue ID {}", savedComment.getId(), issueId);

        return mapCommentToResponse(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByIssueId(Long issueId) throws ResourceNotFoundException {
        log.debug("Fetching comments for issue ID: {}", issueId);

        // 1. Check if issue exists (optional, but good practice)
        if (!issueRepository.existsById(issueId)) {
            throw new ResourceNotFoundException("Issue not found with ID: " + issueId);
        }

        // 2. Fetch comments using the repository method (includes ordering)
        List<Comment> comments = commentRepository.findByIssueIdOrderByCreatedAtAsc(issueId);

        return comments.stream()
                .map(this::mapCommentToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) throws ResourceNotFoundException {
        User currentUser = getCurrentUser();
        log.warn("User '{}' attempting to DELETE comment with ID: {}", currentUser.getEmail(), commentId);

        Comment commentToDelete = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));

        // --- Authorization Check ---
        // Allow deletion only if the current user is the author OR an admin
        if (!commentToDelete.getAuthor().getId().equals(currentUser.getId()) && !isUserAdmin(currentUser)) {
            log.warn("Authorization failed: User '{}' is not author or admin for comment ID {}", currentUser.getEmail(), commentId);
            throw new AccessDeniedException("User does not have permission to delete this comment");
        }
        // --- End Authorization Check ---

        commentRepository.delete(commentToDelete);
        log.warn("Comment with ID {} deleted successfully by user '{}'.", commentId, currentUser.getEmail());
    }


    // ========================================================================
    // Helper Methods (Consider moving common ones like getCurrentUser, isUserAdmin to a separate utility/service)
    // ========================================================================

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("No authenticated user found in security context.");
        }
        String userEmail = (String) authentication.getPrincipal();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user '" + userEmail + "' not found in database. Data inconsistency?"));
    }

    private boolean isUserAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(RoleName.ROLE_ADMIN));
    }

    private CommentResponse mapCommentToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setBody(comment.getBody());
        response.setIssueId(comment.getIssue().getId());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());

        if (comment.getAuthor() != null) {
            response.setAuthor(mapUserToSummaryDto(comment.getAuthor()));
        }

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
