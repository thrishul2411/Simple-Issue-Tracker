package com.sit.simpleissuetracker.repository;

import com.sit.simpleissuetracker.modals.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    public List<Comment> findByIssueIdOrderByCreatedAtAsc(Long issueId);
    /**
     * Counts the number of comments associated with a specific issue.
     * @param issueId The ID of the issue.
     * @return The count of comments for that issue.
     */
    long countByIssueId(Long issueId);
}
