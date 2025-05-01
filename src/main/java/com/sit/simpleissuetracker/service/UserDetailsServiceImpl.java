package com.sit.simpleissuetracker.service;

import com.sit.simpleissuetracker.modals.User;
import com.sit.simpleissuetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Important for lazy loading roles

import java.util.Set;
import java.util.stream.Collectors;

@Service // Mark this as a Spring service component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Override
    @Transactional(readOnly = true) // Read-only transaction is sufficient
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Attempting to load user by email: {}", email);

        // 1. Find the user by email using the repository method
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        log.debug("User found: {}", email);

        // 2. Convert User roles (Set<Role>) to Spring Security authorities (Set<GrantedAuthority>)
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name())) // ROLE_USER, ROLE_ADMIN
                .collect(Collectors.toSet());

        log.debug("User {} authorities: {}", email, authorities);

        // 3. Return Spring Security's User object (implements UserDetails)
        // It requires username, password (hashed!), and authorities.
        // Additional flags control account status (enabled, locked, expired) - true for now.
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // Use email as the 'username' for Spring Security context
                user.getPassword(), // The HASHED password from the database
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities // The granted authorities (roles)
        );
    }
}