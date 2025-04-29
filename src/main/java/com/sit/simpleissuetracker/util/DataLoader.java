package com.sit.simpleissuetracker.util;

import com.sit.simpleissuetracker.modals.*; // Import all entities
import com.sit.simpleissuetracker.repository.*; // Import all repositories
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile; // To activate only in specific profiles
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Override
    @Transactional // Ensures all saves happen in one transaction
    public void run(String... args) throws Exception {
        log.info("DataLoader starting (Profile Active)...");

        // --- 1. Create Roles ---
        Role userRole = createRoleIfNotFound(RoleName.ROLE_USER);
        Role adminRole = createRoleIfNotFound(RoleName.ROLE_ADMIN);
        log.info("Roles checked/created.");

        // --- 2. Create Users ---
        User adminUser = null;
        User regularUser = null;
        User anotherUser = null;

        if (userRepository.count() == 0) {
            log.info("Creating sample users...");

            adminUser = new User();
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword(passwordEncoder.encode("password123"));
            adminUser.setRoles(Set.of(userRole, adminRole));
            adminUser = userRepository.save(adminUser); // Save and re-assign to get ID
            log.info("Created admin user: {}", adminUser.getEmail());

            regularUser = new User();
            regularUser.setFirstName("Regular");
            regularUser.setLastName("User");
            regularUser.setEmail("user@example.com");
            regularUser.setPassword(passwordEncoder.encode("password123"));
            regularUser.setRoles(Set.of(userRole));
            regularUser = userRepository.save(regularUser); // Save and re-assign
            log.info("Created regular user: {}", regularUser.getEmail());

            anotherUser = new User();
            anotherUser.setFirstName("Another");
            anotherUser.setLastName("Dev");
            anotherUser.setEmail("dev@example.com");
            anotherUser.setPassword(passwordEncoder.encode("password123"));
            anotherUser.setRoles(Set.of(userRole));
            anotherUser = userRepository.save(anotherUser); // Save and re-assign
            log.info("Created another user: {}", anotherUser.getEmail());

        } else {
            log.info("Users already exist, fetching existing ones for relationships.");
            // Fetch if they already exist to ensure we have user objects for subsequent steps
            adminUser = userRepository.findByEmail("admin@example.com").orElse(null);
            regularUser = userRepository.findByEmail("user@example.com").orElse(null);
            anotherUser = userRepository.findByEmail("dev@example.com").orElse(null);
            if(adminUser == null || regularUser == null || anotherUser == null) {
                log.error("Could not find essential seed users! Aborting data loading.");
                return; // Or throw exception
            }
        }

        // --- 3. Create Projects ---
        Project projectPhoenix = null;
        Project projectOmega = null;

        if (projectRepository.count() == 0) {
            log.info("Creating sample projects...");

            projectPhoenix = new Project();
            projectPhoenix.setName("Project Phoenix");
            projectPhoenix.setDescription("Rebuilding the core infrastructure.");
            projectPhoenix.setOwner(adminUser); // Admin owns Phoenix
            projectPhoenix = projectRepository.save(projectPhoenix);
            log.info("Created project: {}", projectPhoenix.getName());

            projectOmega = new Project();
            projectOmega.setName("Project Omega");
            projectOmega.setDescription("Developing the next generation features.");
            projectOmega.setOwner(regularUser); // Regular user owns Omega
            projectOmega = projectRepository.save(projectOmega);
            log.info("Created project: {}", projectOmega.getName());
        } else {
            log.info("Projects already exist, fetching existing ones.");
            // Crude fetch, assumes only these two exist or you know their IDs/names
            // A better approach might query by name if names are unique/stable
            List<Project> projects = projectRepository.findAll();
            projectPhoenix = projects.stream().filter(p -> "Project Phoenix".equals(p.getName())).findFirst().orElse(null);
            projectOmega = projects.stream().filter(p -> "Project Omega".equals(p.getName())).findFirst().orElse(null);
            if(projectPhoenix == null || projectOmega == null) {
                log.error("Could not find essential seed projects! Aborting further data loading.");
                return; // Or throw exception
            }
        }

        // --- 4. Create Issues ---
        Issue issue1 = null;
        Issue issue2 = null;
        Issue issue3 = null;

        if (issueRepository.count() == 0) {
            log.info("Creating sample issues...");

            issue1 = new Issue();
            issue1.setTitle("Setup database connection pool");
            issue1.setDescription("Configure HikariCP for optimal performance.");
            issue1.setProject(projectPhoenix);
            issue1.setReporter(adminUser);
            issue1.setAssignee(anotherUser); // Assign to 'dev' user
            issue1.setStatus(IssueStatus.OPEN);
            issue1.setPriority(IssuePriority.HIGH);
            issue1 = issueRepository.save(issue1);
            log.info("Created issue: '{}' in project '{}'", issue1.getTitle(), projectPhoenix.getName());

            issue2 = new Issue();
            issue2.setTitle("Implement user authentication API");
            issue2.setDescription("Create endpoints for user login and registration using Spring Security.");
            issue2.setProject(projectPhoenix);
            issue2.setReporter(adminUser);
            issue2.setAssignee(anotherUser); // Assign to 'dev' user
            issue2.setStatus(IssueStatus.IN_PROGRESS);
            issue2.setPriority(IssuePriority.CRITICAL);
            issue2 = issueRepository.save(issue2);
            log.info("Created issue: '{}' in project '{}'", issue2.getTitle(), projectPhoenix.getName());

            issue3 = new Issue();
            issue3.setTitle("Design UI mockups for dashboard");
            issue3.setDescription("Create wireframes and mockups for the main user dashboard.");
            issue3.setProject(projectOmega);
            issue3.setReporter(regularUser);
            // No assignee initially
            issue3.setStatus(IssueStatus.OPEN);
            issue3.setPriority(IssuePriority.MEDIUM);
            issue3 = issueRepository.save(issue3);
            log.info("Created issue: '{}' in project '{}'", issue3.getTitle(), projectOmega.getName());

        } else {
            log.info("Issues already exist, fetching existing ones for comments.");
            // Again, crude fetch, assumes these exist or query by title/ID
            List<Issue> issues = issueRepository.findAll(); // Inefficient for many issues
            issue1 = issues.stream().filter(i -> "Setup database connection pool".equals(i.getTitle())).findFirst().orElse(null);
            issue2 = issues.stream().filter(i -> "Implement user authentication API".equals(i.getTitle())).findFirst().orElse(null);
            issue3 = issues.stream().filter(i -> "Design UI mockups for dashboard".equals(i.getTitle())).findFirst().orElse(null);
            // Add null checks if needed
        }

        // --- 5. Create Comments ---
        if (commentRepository.count() == 0 && issue1 != null && issue2 != null) { // Check if issues were loaded/created
            log.info("Creating sample comments...");

            Comment comment1 = new Comment();
            comment1.setBody("We should check the timeout settings carefully.");
            comment1.setIssue(issue1); // Comment on issue 1
            comment1.setAuthor(adminUser);
            commentRepository.save(comment1);

            Comment comment2 = new Comment();
            comment2.setBody("Make sure to handle password hashing correctly during registration.");
            comment2.setIssue(issue2); // Comment on issue 2
            comment2.setAuthor(regularUser);
            commentRepository.save(comment2);

            Comment comment3 = new Comment();
            comment3.setBody("I've started working on the JWT implementation for this.");
            comment3.setIssue(issue2); // Another comment on issue 2
            comment3.setAuthor(anotherUser);
            commentRepository.save(comment3);
            log.info("Created sample comments.");

        } else if(commentRepository.count() > 0) {
            log.info("Comments already exist, skipping creation.");
        } else {
            log.warn("Skipping comment creation because required issues were not found.");
        }

        log.info("DataLoader finished.");
    }

    // Helper method to avoid duplicating role creation logic
    private Role createRoleIfNotFound(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    log.info("Creating role: {}", roleName);
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    return roleRepository.save(newRole);
                });
    }
}