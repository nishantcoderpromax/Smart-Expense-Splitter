package com.splitter.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "recurring_expense_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"recurring_expense_id", "user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecurringExpenseParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_expense_id", nullable = false)
    private RecurringExpense recurringExpense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "share_value")
    private BigDecimal shareValue; // same meaning as ExpenseShare.shareValue — null for EQUAL
}

