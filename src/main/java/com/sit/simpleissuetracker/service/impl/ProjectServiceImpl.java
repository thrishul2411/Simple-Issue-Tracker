package com.sit.simpleissuetracker.service.impl;

import com.sit.simpleissuetracker.dto.ProjectRequest;
import com.sit.simpleissuetracker.dto.ProjectResponse;
import com.sit.simpleissuetracker.exception.ResourceNotFoundException;
import com.sit.simpleissuetracker.modals.Project;
import com.sit.simpleissuetracker.modals.RoleName;
import com.sit.simpleissuetracker.modals.User;
import com.sit.simpleissuetracker.repository.IssueRepository;
import com.sit.simpleissuetracker.repository.ProjectRepository;
import com.sit.simpleissuetracker.repository.UserRepository;
import com.sit.simpleissuetracker.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Can be thrown by getCurrentUser
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final IssueRepository issueRepository; // Inject to check issues before project deletion

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest projectRequest) {
        User currentUser = getCurrentUser();
        log.info("User '{}' creating project with name '{}'", currentUser.getEmail(), projectRequest.getName());

        Project project = new Project();
        project.setName(projectRequest.getName());
        project.setDescription(projectRequest.getDescription());
        project.setOwner(currentUser); // Set owner to the logged-in user

        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully with ID: {}", savedProject.getId());

        return mapProjectToResponse(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        log.debug("Fetching all projects");
        // Note: No access control applied here yet. Returns ALL projects.
        return projectRepository.findAll().stream()
                .map(this::mapProjectToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId) throws ResourceNotFoundException {
        log.debug("Fetching project with ID: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
        // Note: No access control applied here yet.
        return mapProjectToResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest projectRequest) throws ResourceNotFoundException {
        // Authorization check should happen here or in Controller using @PreAuthorize
        User currentUser = getCurrentUser(); // Needed for logging/potential auth checks
        log.info("User '{}' attempting to update project with ID: {}", currentUser.getEmail(), projectId);

        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        // Add authorization logic here if needed:
         if (!existingProject.getOwner().getId().equals(currentUser.getId()) && !isUserAdmin(currentUser)) {
             throw new AccessDeniedException("User does not have permission to update this project");
         }

        existingProject.setName(projectRequest.getName());
        existingProject.setDescription(projectRequest.getDescription());
        // Owner typically doesn't change on update, unless explicitly allowed

        Project updatedProject = projectRepository.save(existingProject);
        log.info("Project with ID {} updated successfully.", updatedProject.getId());

        return mapProjectToResponse(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId) throws ResourceNotFoundException {
        User currentUser = getCurrentUser();
        log.warn("User '{}' attempting to DELETE project with ID: {}", currentUser.getEmail(), projectId);

        Project projectToDelete = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        if (!projectToDelete.getOwner().getId().equals(currentUser.getId()) && !isUserAdmin(currentUser)) {
            throw new AccessDeniedException("User does not have permission to update this project");
        }

        // --- Deletion Constraint Check (Example) ---
        // Decide what happens if a project has issues.
        // Option 1: Prevent deletion if issues exist
        long issueCount = issueRepository.countByProjectId(projectId);
        if (issueCount > 0) {
            log.error("Deletion failed: Project {} cannot be deleted because it contains {} issues.", projectId, issueCount);
            // Use a specific exception or BadRequestException
            throw new IllegalStateException("Cannot delete project with ID " + projectId + " as it contains active issues.");
        }

        // Option 2: Allow deletion (requires cascade settings on Project entity's issues field)
        // If Project entity has `@OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE)`
        // Then deleting the project will automatically delete its issues.

        // Proceed with deletion if checks pass
        projectRepository.delete(projectToDelete);
        log.warn("Project with ID {} deleted successfully by user '{}'.", projectId, currentUser.getEmail());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // This case should ideally be handled by Spring Security filters earlier
            // but good to have a safeguard.
            throw new IllegalStateException("No authenticated user found in security context.");
        }
        // The principal *should* be the email string we put in UserDetailsServiceImpl
        String userEmail = (String) authentication.getPrincipal();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user '" + userEmail + "' not found in database. Data inconsistency?"));
    }


    // --- Helper: Mapper (Consider MapStruct) ---
    private ProjectResponse mapProjectToResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setCreatedAt(project.getCreatedAt()); // Assuming @CreationTimestamp
        response.setUpdatedAt(project.getUpdatedAt()); // Assuming @UpdateTimestamp

        // Handle owner mapping
        if (project.getOwner() != null) {
            response.setOwnerId(project.getOwner().getId());
            // Avoid creating full UserResponse here, use UserSummaryDto or just name/email
            response.setOwnerName(project.getOwner().getFirstName() + " " + project.getOwner().getLastName());
            // Or create and set a UserSummaryDto:
            // response.setOwner(new UserSummaryDto(project.getOwner().getId(),
            //          project.getOwner().getFirstName() + " " + project.getOwner().getLastName(),
            //          project.getOwner().getEmail()));
        } else {
            response.setOwnerId(null);
            response.setOwnerName(null); // Or empty string
        }
        return response;
    }

    private boolean isUserAdmin(User user) {
        // Check if the user's roles collection contains the ROLE_ADMIN
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(RoleName.ROLE_ADMIN));
    }

}