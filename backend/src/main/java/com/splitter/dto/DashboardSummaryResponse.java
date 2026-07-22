package com.splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardSummaryResponse {
    private BigDecimal totalOwedToYou;   // sum of positive balances across groups
    private BigDecimal totalYouOwe;      // sum of negative balances across groups (as positive number)
    private BigDecimal netOverall;       // totalOwedToYou - totalYouOwe
    private List<GroupBalanceSummary> perGroup;
}

