package com.splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MonthlySpendResponse {
    private String month; // e.g. "2026-07"
    private BigDecimal total;
}