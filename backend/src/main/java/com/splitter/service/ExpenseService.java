package com.splitter.service;

import com.splitter.dto.*;
import com.splitter.entity.Expense;
import com.splitter.entity.ExpenseShare;
import com.splitter.entity.Group;
import com.splitter.entity.User;
import com.splitter.enums.ActivityType;
import com.splitter.exception.ApiException;
import com.splitter.payload.ExpenseSearchCriteria;
import com.splitter.repository.CategoryRepository;
import com.splitter.repository.ExpenseRepository;
import com.splitter.repository.GroupMemberRepository;
import com.splitter.repository.GroupRepository;
import com.splitter.repository.UserRepository;
import com.splitter.security.CurrentUser;
import com.splitter.specification.ExpenseSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
 
@Service
@RequiredArgsConstructor
public class ExpenseService {
 
    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SplitCalculator splitCalculator;
    private final GroupEventPublisher eventPublisher;
    private final ActivityLogService activityLogService;
 
    @Transactional
    public ExpenseResponse addExpense(Long groupId, ExpenseRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Group not found"));
 
        User paidBy = userRepository.findById(request.getPaidBy())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payer not found"));
 
        // every participant (and the payer) must actually belong to the group
        requireMember(groupId, paidBy.getId());
        request.getParticipants().forEach(p -> requireMember(groupId, p.getUserId()));
 
        Map<Long, BigDecimal> owedByUser = splitCalculator.calculate(
                request.getAmount(), request.getSplitType(), request.getParticipants());
 
        Expense expense = Expense.builder()
                .group(group)
                .paidBy(paidBy)
                .description(request.getDescription())
                .amount(request.getAmount())
                .splitType(request.getSplitType())
                .category(request.getCategoryId() == null ? null :
                        categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found")))
                .build();
 
        request.getParticipants().forEach(p -> {
            User participant = userRepository.findById(p.getUserId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Participant not found: " + p.getUserId()));
            expense.getShares().add(ExpenseShare.builder()
                    .expense(expense)
                    .user(participant)
                    .shareValue(p.getValue())
                    .owedAmount(owedByUser.get(p.getUserId()))
                    .build());
        });
 
        expenseRepository.save(expense); // cascades to shares
 
        User actor = userRepository.findByEmail(CurrentUser.email()).orElse(paidBy);
        activityLogService.record(group, actor, ActivityType.EXPENSE_ADDED,
                String.format("%s added %s for \"%s\"", paidBy.getName(), request.getAmount(), request.getDescription()));
        eventPublisher.publish(groupId, "EXPENSE_ADDED");
 
        return toResponse(expense);
    }
 
    public List<ExpenseResponse> listForGroup(Long groupId) {
        return expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId)
                .stream().map(this::toResponse).toList();
    }
 
    public void deleteExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Expense not found"));
        Long groupId = expense.getGroup().getId();
        User actor = userRepository.findByEmail(CurrentUser.email()).orElse(expense.getPaidBy());
 
        expenseRepository.deleteById(expenseId);
 
        activityLogService.record(expense.getGroup(), actor, ActivityType.EXPENSE_DELETED,
                String.format("%s removed the expense \"%s\"", actor.getName(), expense.getDescription()));
        eventPublisher.publish(groupId, "EXPENSE_DELETED");
    }
 
    /**
     * Search/filter/sort/paginate expenses within a group. Every filter is optional —
     * ExpenseSpecification skips null filters, so this handles any combination
     * (just a keyword, just a category, a full filter set, etc.) with one query path.
     */
    public PageResponse<ExpenseResponse> search(Long groupId, ExpenseSearchCriteria criteria, Pageable pageable) {
        Specification<Expense> spec = Specification
                .where(ExpenseSpecification.belongsToGroup(groupId))
                .and(ExpenseSpecification.descriptionContains(criteria.description()))
                .and(ExpenseSpecification.hasCategory(criteria.categoryId()))
                .and(ExpenseSpecification.paidByUser(criteria.paidBy()))
                .and(ExpenseSpecification.amountAtLeast(criteria.minAmount()))
                .and(ExpenseSpecification.amountAtMost(criteria.maxAmount()));
 
        Page<Expense> page = expenseRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }
 
    private void requireMember(Long groupId, Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "User " + userId + " is not a member of this group");
        }
    }
 
    private ExpenseResponse toResponse(Expense expense) {
        List<ExpenseShareResponse> shares = expense.getShares().stream()
                .map(s -> new ExpenseShareResponse(s.getUser().getId(), s.getUser().getName(), s.getOwedAmount()))
                .toList();
        return new ExpenseResponse(
                expense.getId(), expense.getDescription(), expense.getAmount(), expense.getSplitType(),
                expense.getPaidBy().getName(), expense.getPaidBy().getId(),
                expense.getCategory() == null ? null : expense.getCategory().getName(),
                expense.getCreatedAt(), shares);
    }
}

