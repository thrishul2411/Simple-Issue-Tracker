package com.sit.simpleissuetracker.repository;

import com.sit.simpleissuetracker.modals.Issue;
import com.sit.simpleissuetracker.modals.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByProjectId(Long projectId);
    List<Issue> findByAssigneeId(Long assigneeId);
    List<Issue> findByReporterId(Long reporterId);
    List<Issue> findByProjectIdAndStatus(Long projectId, IssueStatus status);

    /**
     * Counts the number of issues associated with a specific project.
     * More efficient than fetching all issues and checking the list size.
     * @param projectId The ID of the project.
     * @return The count of issues in that project.
     */
    long countByProjectId(Long projectId);
}
