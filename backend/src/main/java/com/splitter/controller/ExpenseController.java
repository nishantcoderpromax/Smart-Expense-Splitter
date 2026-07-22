package com.splitter.controller;

import com.splitter.dto.ExpenseRequest;
import com.splitter.dto.ExpenseResponse;
import com.splitter.dto.PageResponse;
import com.splitter.payload.ExpenseSearchCriteria;
import com.splitter.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/groups/{groupId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> add(@PathVariable Long groupId, @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.addExpense(groupId, request));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> list(@PathVariable Long groupId) {
        return ResponseEntity.ok(expenseService.listForGroup(groupId));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> delete(@PathVariable Long groupId, @PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search + filter + sort + paginate.
     * e.g. GET /groups/1/expenses/search?description=pizza&minAmount=100&sortBy=amount&sortDir=desc&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponse<ExpenseResponse>> search(
            @PathVariable Long groupId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long paidBy,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        ExpenseSearchCriteria criteria = new ExpenseSearchCriteria(description, categoryId, paidBy, minAmount, maxAmount);
        return ResponseEntity.ok(expenseService.search(groupId, criteria, pageable));
    }
}
