package com.splitter.dto;

import com.splitter.enums.RecurrenceFrequency;
import com.splitter.enums.SplitType;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class RecurringExpenseResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private SplitType splitType;
    private String paidByName;
    private String categoryName; // null if uncategorized
    private RecurrenceFrequency frequency;
    private LocalDate nextRunDate;
    private boolean active;
}
