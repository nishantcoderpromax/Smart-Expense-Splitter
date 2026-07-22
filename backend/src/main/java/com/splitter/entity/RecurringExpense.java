package com.splitter.entity;

import com.splitter.enums.RecurrenceFrequency;
import com.splitter.enums.SplitType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recurring_expenses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecurringExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by", nullable = false)
    private User paidBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    private String description;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_type")
    private SplitType splitType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    private RecurrenceFrequency frequency;

    @Column(name = "next_run_date")
    private LocalDate nextRunDate;

    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "recurringExpense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecurringExpenseParticipant> participants = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
