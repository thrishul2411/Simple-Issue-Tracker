package com.sit.simpleissuetracker.repository;

import com.sit.simpleissuetracker.modals.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Integer> {
}
