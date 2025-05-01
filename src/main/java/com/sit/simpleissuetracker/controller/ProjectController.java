package com.sit.simpleissuetracker.controller;

import com.sit.simpleissuetracker.dto.ProjectRequest;
import com.sit.simpleissuetracker.dto.ProjectResponse;
import com.sit.simpleissuetracker.exception.ResourceNotFoundException;
import com.sit.simpleissuetracker.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // Import Spring Security exception
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest projectRequest) {
        log.info("Received request to create project: {}", projectRequest.getName());
        // Assuming createProject handles setting the owner based on authenticated user
        ProjectResponse createdProject = projectService.createProject(projectRequest);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
        // Error handling for potential exceptions from service layer can be added here
        // or handled globally via @ControllerAdvice
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        log.debug("Received request to get all projects");
        List<ProjectResponse> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long projectId) {
        log.debug("Received request to get project by ID: {}", projectId);
        try {
            ProjectResponse project = projectService.getProjectById(projectId);
            return ResponseEntity.ok(project);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long projectId,
                                                         @Valid @RequestBody ProjectRequest projectRequest) {
        log.info("Received request to update project ID: {}", projectId);
        try {
            ProjectResponse updatedProject = projectService.updateProject(projectId, projectRequest);
            return ResponseEntity.ok(updatedProject);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error updating project {}: {}", projectId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred updating the project.", e);
        }
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        log.warn("Received request to DELETE project ID: {}", projectId);
        try {
            projectService.deleteProject(projectId);
            return ResponseEntity.noContent().build(); // Return 204 No Content
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (IllegalStateException e) { // Catch the exception for deletion constraint
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e); // Return 409 Conflict
        } catch (Exception e) {
            log.error("Error deleting project {}: {}", projectId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred deleting the project.", e);
        }
    }
}