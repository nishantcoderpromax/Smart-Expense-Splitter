package com.splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ExpenseShareResponse {
    private Long userId;
    private String name;
    private BigDecimal owedAmount;
}
