package com.splitter.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "expense_shares", uniqueConstraints = @UniqueConstraint(columnNames = {"expense_id", "user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExpenseShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "share_value")
    private BigDecimal shareValue; // raw input (exact amount / percentage / share count); null for EQUAL

    @Column(name = "owed_amount")
    private BigDecimal owedAmount; // final computed amount, always in currency units
}
