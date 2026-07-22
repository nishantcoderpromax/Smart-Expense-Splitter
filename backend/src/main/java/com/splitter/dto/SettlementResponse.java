package com.splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SettlementResponse {
    private Long fromUserId;
    private String fromName;
    private Long toUserId;
    private String toName;
    private BigDecimal amount; // fromUser should pay toUser this amount
}
