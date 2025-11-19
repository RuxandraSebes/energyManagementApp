// demoP/security/SecurityConfig.java and demoD/security/SecurityConfig.java

package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> {
                    // Public/Internal Endpoints
                    auth.requestMatchers("/auth/**").permitAll() // Auth is handled by the Auth service
                            // Internal-only endpoint (used by Auth service for registration)
                            .requestMatchers(HttpMethod.POST, "/people/internal-auth-insert").permitAll()
                            .requestMatchers(HttpMethod.DELETE, "/devices/user/**").permitAll() // Internal only for People Service delete cascade
                            .requestMatchers(HttpMethod.GET, "/people/by-auth/**").permitAll();
                    // --- ADMIN ONLY access (CRUD on all resources) ---
                    auth.requestMatchers(HttpMethod.POST, "/people", "/devices").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/people/**", "/devices/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/people/**", "/devices/**").hasRole("ADMIN");

                    // --- AUTHENTICATED ACCESS (User and Admin) ---
                    // Specific authorization logic will be handled inside the controllers/services
                    auth.requestMatchers("/people/**", "/devices/**").authenticated();

                    // Fallback for any other request
                    auth.anyRequest().authenticated();
                })
                // Make session stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Add the JWT filter before the standard Spring Security filter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}