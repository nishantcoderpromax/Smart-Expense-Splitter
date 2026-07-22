package com.splitter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
 
/**
 * Kept separate from SecurityConfig on purpose: SecurityConfig depends (via
 * constructor injection) on OAuth2LoginSuccessHandler -> AuthService ->
 * PasswordEncoder. If PasswordEncoder were a @Bean method inside SecurityConfig
 * itself, Spring couldn't construct SecurityConfig without it already existing,
 * creating a circular dependency. A standalone config class has no such
 * constructor dependencies, so it breaks the cycle.
 */
@Configuration
public class PasswordEncoderConfig {
 
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

