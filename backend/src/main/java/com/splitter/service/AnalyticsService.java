package com.splitter.service;

import com.splitter.dto.CategorySpendResponse;
import com.splitter.dto.MonthlySpendResponse;
import com.splitter.entity.Expense;
import com.splitter.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    /** Total spent per calendar month, oldest to newest, for a group. */
    public List<MonthlySpendResponse> monthlySpend(Long groupId) {
        List<Expense> expenses = expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId);

        // TreeMap keeps months sorted chronologically without a separate sort step
        Map<String, BigDecimal> totals = new TreeMap<>();
        for (Expense e : expenses) {
            String month = e.getCreatedAt().format(MONTH_FORMAT);
            totals.merge(month, e.getAmount(), BigDecimal::add);
        }

        return totals.entrySet().stream()
                .map(entry -> new MonthlySpendResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    /** Total spent per category for a group (uncategorized expenses grouped together). */
    public List<CategorySpendResponse> categorySpend(Long groupId) {
        List<Expense> expenses = expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId);

        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (Expense e : expenses) {
            String category = e.getCategory() != null ? e.getCategory().getName() : "Uncategorized";
            totals.merge(category, e.getAmount(), BigDecimal::add);
        }

        return totals.entrySet().stream()
                .map(entry -> new CategorySpendResponse(entry.getKey(), entry.getValue()))
                .toList();
    }
}
