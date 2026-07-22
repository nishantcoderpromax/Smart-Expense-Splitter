package com.splitter.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private String name;
 
    @Column(unique = true, nullable = false)
    private String email;
 
    private String password; // stored as BCrypt hash, never plain text
 
    // Defaults true so existing/OAuth accounts are unaffected; AuthService
    // explicitly sets this false for brand-new email/password registrations.
    @Builder.Default
    @Column(name = "email_verified")
    private boolean emailVerified = true;
 
    @Column(name = "created_at")
    private LocalDateTime createdAt;
 
    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
