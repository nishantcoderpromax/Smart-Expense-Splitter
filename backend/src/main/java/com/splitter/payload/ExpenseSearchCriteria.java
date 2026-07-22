package com.splitter.payload;

import java.math.BigDecimal;

/** All fields optional — a null field means "don't filter on this". */
public record ExpenseSearchCriteria(
        String description,
        Long categoryId,
        Long paidBy,
        BigDecimal minAmount,
        BigDecimal maxAmount
) {}
