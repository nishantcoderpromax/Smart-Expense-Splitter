package com.splitter.controller;

import com.splitter.dto.ExpenseResponse;
import com.splitter.dto.RecurringExpenseRequest;
import com.splitter.dto.RecurringExpenseResponse;
import com.splitter.service.RecurringExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups/{groupId}/recurring-expenses")
@RequiredArgsConstructor
public class RecurringExpenseController {

    private final RecurringExpenseService recurringExpenseService;

    @PostMapping
    public ResponseEntity<RecurringExpenseResponse> create(@PathVariable Long groupId, @Valid @RequestBody RecurringExpenseRequest request) {
        return ResponseEntity.ok(recurringExpenseService.create(groupId, request));
    }

    @GetMapping
    public ResponseEntity<List<RecurringExpenseResponse>> list(@PathVariable Long groupId) {
        return ResponseEntity.ok(recurringExpenseService.listForGroup(groupId));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> setActive(@PathVariable Long groupId, @PathVariable Long id, @RequestParam boolean active) {
        recurringExpenseService.setActive(id, active);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long groupId, @PathVariable Long id) {
        recurringExpenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Generates this cycle's expense immediately instead of waiting for the
     *  daily scheduler — mainly so the feature can be demoed/tested on the spot. */
    @PostMapping("/{id}/run-now")
    public ResponseEntity<ExpenseResponse> runNow(@PathVariable Long groupId, @PathVariable Long id) {
        return ResponseEntity.ok(recurringExpenseService.runNow(id));
    }
}