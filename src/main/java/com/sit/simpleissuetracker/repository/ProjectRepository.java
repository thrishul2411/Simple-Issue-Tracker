package com.sit.simpleissuetracker.repository;

import com.sit.simpleissuetracker.modals.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
