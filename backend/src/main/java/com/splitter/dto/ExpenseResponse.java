package com.splitter.dto;

import com.splitter.enums.SplitType;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ExpenseResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private SplitType splitType;
    private String paidByName;
    private Long paidByUserId;
    private String categoryName; // null if uncategorized
    private LocalDateTime createdAt;
    private List<ExpenseShareResponse> shares;
}