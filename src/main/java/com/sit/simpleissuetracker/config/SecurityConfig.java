package com.sit.simpleissuetracker.config;

import com.sit.simpleissuetracker.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // For @PreAuthorize etc.
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // For CSRF disable lambda
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // If using H2 console

@Configuration
@EnableWebSecurity // Enable Spring Security's web security support
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true) // Enable method-level security (@PreAuthorize etc.)
@RequiredArgsConstructor // Lombok constructor injection
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService; // Inject your UserDetailsService

    // PasswordEncoder Bean (already defined)
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager Bean (Needed for programmatic login if required, but often implicitly handled)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean // Define the main security filter chain
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // --- CSRF Configuration ---
                // Disable CSRF protection. Common for stateless REST APIs (like JWT).
                // For session-based auth, enabling CSRF with proper token handling (e.g., CsrfTokenRepository) is safer,
                // but complicates API clients. Disabling is simpler for now, but be aware of implications.
                .csrf(AbstractHttpConfigurer::disable) // Using lambda for newer Spring Security versions

                // --- Authorization Rules ---
                .authorizeHttpRequests(authorize -> authorize
                        // Publicly accessible endpoints
                        .requestMatchers("/error").permitAll() // Allow Spring Boot error pages
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll() // Allow registration
                        // Allow H2 console access *only* if H2 profile is active (more secure)
                        // Alternatively, remove this rule entirely if not using H2 or if security is paramount
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll() // Allow H2 console access if needed

                        // Secure API endpoints - require authentication for everything under /api/v1
                        .requestMatchers("/api/v1/**").authenticated()

                        // Default: Deny all other requests? Or Authenticate? Authenticate is safer default.
                        .anyRequest().authenticated()
                )

                // --- Authentication Mechanisms ---
                // Tell Spring Security how to use our UserDetailsService
                .userDetailsService(userDetailsService)

                // Configure Form Login (standard browser-based login form)
                .formLogin(form -> form
                        .loginProcessingUrl("/login") // The URL Spring Security listens on for login POST requests
                        // .loginPage("/your-login-page") // Optional: Specify custom login page URL if you have a frontend
                        .defaultSuccessUrl("/api/v1/users/me", true) // Redirect to user profile on successful login (adjust as needed)
                        .failureUrl("/login?error=true") // Redirect back to login on failure (adjust as needed)
                        .permitAll() // Allow access to the login processing URL itself
                )
                // Configure Logout
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST")) // Specify logout URL and method
                        .logoutSuccessUrl("/login?logout=true") // Redirect on successful logout
                        .invalidateHttpSession(true) // Invalidate session
                        .deleteCookies("JSESSIONID") // Delete session cookie
                        .permitAll() // Allow access to the logout URL
                )

                // --- H2 Console Frame Options ---
                // Required to allow H2 console frames if using H2 and frame security headers are enabled
                .headers(headers -> headers
                        .frameOptions(Customizer.withDefaults()).disable() // Allow frames for H2 console - disable frameOptions protection
                );


        return http.build();
    }
}
