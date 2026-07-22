package com.splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CategorySpendResponse {
    private String categoryName; // "Uncategorized" if null
    private BigDecimal total;
}
