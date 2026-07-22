package com.splitter.controller;

import com.splitter.dto.ActivityItemResponse;
import com.splitter.dto.DashboardSummaryResponse;
import com.splitter.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> summary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/activity")
    public ResponseEntity<List<ActivityItemResponse>> activity(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentActivity(limit));
    }
}

