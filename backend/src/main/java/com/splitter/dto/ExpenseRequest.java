package com.splitter.dto;

import com.splitter.enums.SplitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ExpenseRequest {
    @NotBlank
    private String description;

    @NotNull @Positive
    private BigDecimal amount;

    @NotNull
    private SplitType splitType;

    @NotNull
    private Long paidBy; // userId of who paid

    private Long categoryId; // optional

    @NotEmpty
    private List<ParticipantShareRequest> participants; // who owes a share of this expense
}


