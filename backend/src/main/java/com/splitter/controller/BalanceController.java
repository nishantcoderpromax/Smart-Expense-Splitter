package com.splitter.controller;

import com.splitter.dto.*;
import com.splitter.service.BalanceService;
import com.splitter.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups/{groupId}")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;
    private final SettlementService settlementService;

    @GetMapping("/balances")
    public ResponseEntity<List<BalanceResponse>> balances(@PathVariable Long groupId) {
        return ResponseEntity.ok(balanceService.calculateBalances(groupId));
    }

    /** Suggested minimal set of payments to settle everyone up (not yet paid). */
    @GetMapping("/settlements")
    public ResponseEntity<List<SettlementResponse>> suggestedSettlements(@PathVariable Long groupId) {
        return ResponseEntity.ok(balanceService.simplifyDebts(groupId));
    }

    /** Record that a payment actually happened; logged-in user is the payer. */
    @PostMapping("/settlements")
    public ResponseEntity<SettlementHistoryResponse> recordSettlement(
            @PathVariable Long groupId, @Valid @RequestBody RecordSettlementRequest request) {
        return ResponseEntity.ok(settlementService.record(groupId, request));
    }

    @GetMapping("/settlements/history")
    public ResponseEntity<List<SettlementHistoryResponse>> settlementHistory(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementService.history(groupId));
    }
}
