package com.splitter.entity;

import com.splitter.enums.TokenPurpose;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "verification_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VerificationToken {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
 
    @Column(unique = true, nullable = false)
    private String token;
 
    @Enumerated(EnumType.STRING)
    private TokenPurpose purpose;
 
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
 
    @Builder.Default
    private boolean used = false;
 
    @Column(name = "created_at")
    private LocalDateTime createdAt;
 
    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
