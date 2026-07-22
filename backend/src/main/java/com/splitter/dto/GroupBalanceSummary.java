package com.splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class GroupBalanceSummary {
    private Long groupId;
    private String groupName;
    private BigDecimal netBalance; // this user's balance within that one group
}
