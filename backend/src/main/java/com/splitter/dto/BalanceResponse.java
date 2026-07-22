package com.splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BalanceResponse {
    private Long userId;
    private String name;
    // positive = group owes this person (they overpaid), negative = this person owes the group
    private BigDecimal netBalance;
}
