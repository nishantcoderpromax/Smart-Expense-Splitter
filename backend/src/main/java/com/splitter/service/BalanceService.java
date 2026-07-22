package com.splitter.service;

import com.splitter.dto.BalanceResponse;
import com.splitter.dto.SettlementResponse;
import com.splitter.entity.Expense;
import com.splitter.entity.ExpenseShare;
import com.splitter.entity.Settlement;
import com.splitter.entity.User;
import com.splitter.repository.ExpenseRepository;
import com.splitter.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;

    /**
     * Net balance per user in a group, after factoring in both expenses and
     * any settlements (actual payments) already recorded:
     * +ve => the group owes them (they paid more than their share)
     * -ve => they owe the group (their share exceeds what they paid)
     */
    public List<BalanceResponse> calculateBalances(Long groupId) {
        List<Expense> expenses = expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId);

        Map<Long, BigDecimal> net = new LinkedHashMap<>();
        Map<Long, String> names = new LinkedHashMap<>();

        for (Expense expense : expenses) {
            User payer = expense.getPaidBy();
            names.put(payer.getId(), payer.getName());
            net.merge(payer.getId(), expense.getAmount(), BigDecimal::add);

            for (ExpenseShare share : expense.getShares()) {
                User ower = share.getUser();
                names.put(ower.getId(), ower.getName());
                net.merge(ower.getId(), share.getOwedAmount().negate(), BigDecimal::add);
            }
        }

        // A settlement means fromUser already paid toUser, so it reduces
        // fromUser's debt (net moves up) and reduces toUser's credit (net moves down).
        for (Settlement s : settlementRepository.findByGroupIdOrderBySettledAtDesc(groupId)) {
            names.put(s.getFromUser().getId(), s.getFromUser().getName());
            names.put(s.getToUser().getId(), s.getToUser().getName());
            net.merge(s.getFromUser().getId(), s.getAmount(), BigDecimal::add);
            net.merge(s.getToUser().getId(), s.getAmount().negate(), BigDecimal::add);
        }

        List<BalanceResponse> result = new ArrayList<>();
        net.forEach((userId, balance) -> result.add(new BalanceResponse(userId, names.get(userId), balance)));
        return result;
    }

    /**
     * Greedy min-cash-flow: repeatedly match the biggest debtor with the biggest
     * creditor. Minimizes the number of transactions needed to settle everyone up.
     */
    public List<SettlementResponse> simplifyDebts(Long groupId) {
        List<BalanceResponse> balances = calculateBalances(groupId);

        PriorityQueue<BalanceResponse> creditors = new PriorityQueue<>(
                Comparator.comparing(BalanceResponse::getNetBalance).reversed());
        PriorityQueue<BalanceResponse> debtors = new PriorityQueue<>(
                Comparator.comparing(BalanceResponse::getNetBalance));

        for (BalanceResponse b : balances) {
            if (b.getNetBalance().compareTo(BigDecimal.ZERO) > 0) creditors.add(b);
            else if (b.getNetBalance().compareTo(BigDecimal.ZERO) < 0) debtors.add(b);
        }

        List<SettlementResponse> settlements = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            BalanceResponse creditor = creditors.poll();
            BalanceResponse debtor = debtors.poll();

            BigDecimal owed = debtor.getNetBalance().negate(); // positive amount debtor owes
            BigDecimal settledAmount = owed.min(creditor.getNetBalance());

            settlements.add(new SettlementResponse(
                    debtor.getUserId(), debtor.getName(),
                    creditor.getUserId(), creditor.getName(),
                    settledAmount));

            BigDecimal creditorRemaining = creditor.getNetBalance().subtract(settledAmount);
            BigDecimal debtorRemaining = owed.subtract(settledAmount).negate();

            if (creditorRemaining.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new BalanceResponse(creditor.getUserId(), creditor.getName(), creditorRemaining));
            }
            if (debtorRemaining.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new BalanceResponse(debtor.getUserId(), debtor.getName(), debtorRemaining));
            }
        }

        return settlements;
    }
}

