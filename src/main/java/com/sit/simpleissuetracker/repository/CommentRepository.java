package com.sit.simpleissuetracker.repository;

import com.sit.simpleissuetracker.modals.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    public List<Comment> findByIssueIdOrderByCreatedAtAsc(Long issueId);
}
