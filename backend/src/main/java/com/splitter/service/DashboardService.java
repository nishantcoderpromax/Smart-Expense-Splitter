package com.splitter.service;

import com.splitter.dto.*;
import com.splitter.entity.Expense;
import com.splitter.entity.Group;
import com.splitter.entity.User;
import com.splitter.exception.ApiException;
import com.splitter.repository.ExpenseRepository;
import com.splitter.repository.GroupRepository;
import com.splitter.repository.UserRepository;
import com.splitter.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final BalanceService balanceService;

    public DashboardSummaryResponse getSummary() {
        User user = currentUser();
        List<Group> groups = groupRepository.findAllForUser(user.getId());

        BigDecimal owedToYou = BigDecimal.ZERO;
        BigDecimal youOwe = BigDecimal.ZERO;
        List<GroupBalanceSummary> perGroup = new java.util.ArrayList<>();

        for (Group group : groups) {
            BigDecimal myBalance = balanceService.calculateBalances(group.getId()).stream()
                    .filter(b -> b.getUserId().equals(user.getId()))
                    .map(BalanceResponse::getNetBalance)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);

            perGroup.add(new GroupBalanceSummary(group.getId(), group.getName(), myBalance));

            if (myBalance.signum() > 0) owedToYou = owedToYou.add(myBalance);
            else if (myBalance.signum() < 0) youOwe = youOwe.add(myBalance.negate());
        }

        return new DashboardSummaryResponse(owedToYou, youOwe, owedToYou.subtract(youOwe), perGroup);
    }

    public List<ActivityItemResponse> getRecentActivity(int limit) {
        User user = currentUser();
        List<Expense> expenses = expenseRepository.findRecentForUser(user.getId(), PageRequest.of(0, limit));

        return expenses.stream()
                .map(e -> new ActivityItemResponse(
                        e.getGroup().getId(), e.getGroup().getName(), e.getDescription(),
                        e.getAmount(), e.getPaidBy().getName(), e.getCreatedAt()))
                .toList();
    }

    private User currentUser() {
        return userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
