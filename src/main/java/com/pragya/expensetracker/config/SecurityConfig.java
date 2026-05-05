package com.pragya.expensetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF because we are using REST APIs (Postman + frontend JS)
                .csrf(csrf -> csrf.disable())

                // TEMPORARY security rules (for frontend development)
                .authorizeHttpRequests(auth -> auth
                        // Login API must be open
                        .requestMatchers("/auth/**").permitAll()

                        // Expense APIs TEMPORARILY open (JWT will be added later)
                        .requestMatchers("/expenses/**").permitAll()

                        // Chatbot API
                        .requestMatchers("/api/chat/**").permitAll()

                        // Allow frontend static files (index.html, js, css)
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/dashboard.js",
                                "/styles.css",
                                "/**/*.js",
                                "/**/*.css"
                        ).permitAll()

                        // Everything else also allowed for now
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}

