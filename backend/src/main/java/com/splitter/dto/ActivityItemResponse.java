package com.splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ActivityItemResponse {
    private Long groupId;
    private String groupName;
    private String description;
    private BigDecimal amount;
    private String paidByName;
    private LocalDateTime createdAt;
}
