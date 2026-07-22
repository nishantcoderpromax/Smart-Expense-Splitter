package com.splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SettlementHistoryResponse {
    private Long id;
    private String fromName;
    private String toName;
    private BigDecimal amount;
    private LocalDateTime settledAt;
}
