package com.splitter.controller;

import com.splitter.dto.CategorySpendResponse;
import com.splitter.dto.MonthlySpendResponse;
import com.splitter.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/groups/{groupId}/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlySpendResponse>> monthly(@PathVariable Long groupId) {
        return ResponseEntity.ok(analyticsService.monthlySpend(groupId));
    }

    @GetMapping("/category")
    public ResponseEntity<List<CategorySpendResponse>> category(@PathVariable Long groupId) {
        return ResponseEntity.ok(analyticsService.categorySpend(groupId));
    }
}
