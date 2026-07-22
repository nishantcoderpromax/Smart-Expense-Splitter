package com.splitter.entity;

import com.splitter.enums.ActivityType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor; // who performed the action (the logged-in user making the API call)

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private ActivityType actionType;

    // Pre-formatted human-readable text (e.g. "Rohan added ₹500 for Groceries"),
    // computed once at write time so the timeline never needs to re-join/re-derive it.
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}