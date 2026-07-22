package com.splitter.dto;

import com.splitter.enums.RecurrenceFrequency;
import com.splitter.enums.SplitType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class RecurringExpenseRequest {
    @NotBlank
    private String description;

    @NotNull @Positive
    private BigDecimal amount;

    @NotNull
    private SplitType splitType;

    @NotNull
    private Long paidBy;

    private Long categoryId; // optional

    @NotNull
    private RecurrenceFrequency frequency;

    @NotNull
    private LocalDate startDate; // first date this should be generated

    @NotEmpty
    private List<ParticipantShareRequest> participants;
}

