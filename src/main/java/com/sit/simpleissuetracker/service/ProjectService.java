package com.sit.simpleissuetracker.service;

import com.sit.simpleissuetracker.dto.ProjectRequest;
import com.sit.simpleissuetracker.dto.ProjectResponse;
import com.sit.simpleissuetracker.exception.ResourceNotFoundException;

import java.util.List;

public interface ProjectService {

    /**
     * Creates a new project. The owner is set to the currently authenticated user.
     * @param projectRequest DTO containing project details.
     * @return DTO representing the newly created project.
     */
    ProjectResponse createProject(ProjectRequest projectRequest);

    /**
     * Retrieves a list of all projects.
     * (Future enhancement: Add filtering/pagination/access control)
     * @return A list of project DTOs.
     */
    List<ProjectResponse> getAllProjects();

    /**
     * Retrieves a specific project by its ID.
     * @param projectId The ID of the project to retrieve.
     * @return DTO representing the project.
     * @throws ResourceNotFoundException if the project with the given ID is not found.
     */
    ProjectResponse getProjectById(Long projectId) throws ResourceNotFoundException;

    /**
     * Updates an existing project.
     * (Future enhancement: Add authorization check - only owner/admin can update)
     * @param projectId The ID of the project to update.
     * @param projectRequest DTO containing updated project details.
     * @return DTO representing the updated project.
     * @throws ResourceNotFoundException if the project with the given ID is not found.
     */
    ProjectResponse updateProject(Long projectId, ProjectRequest projectRequest) throws ResourceNotFoundException;

    /**
     * Deletes a project by its ID.
     * (Future enhancement: Add authorization check, check for contained issues)
     * @param projectId The ID of the project to delete.
     * @throws ResourceNotFoundException if the project with the given ID is not found.
     * @throws RuntimeException or specific exception if deletion constraints are violated (e.g., project has issues).
     */
    void deleteProject(Long projectId) throws ResourceNotFoundException;

    // --- Helper method to get current user (can be defined elsewhere too) ---
    // User getCurrentUser(); // We might implement this privately or in a utility class
}