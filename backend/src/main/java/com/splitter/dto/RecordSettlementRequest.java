package com.splitter.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RecordSettlementRequest {
    @NotNull
    private Long toUserId; // who received the payment

    @NotNull @Positive
    private BigDecimal amount;
}