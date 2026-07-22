package com.splitter.service;

import com.splitter.dto.*;
import com.splitter.entity.*;
import com.splitter.exception.ApiException;
import com.splitter.repository.*;
import com.splitter.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecurringExpenseService {

    private final RecurringExpenseRepository recurringExpenseRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseService expenseService; // reused for actually generating each cycle's expense

    public RecurringExpenseResponse create(Long groupId, RecurringExpenseRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Group not found"));
        User paidBy = userRepository.findById(request.getPaidBy())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payer not found"));
        User creator = userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));

        requireMember(groupId, paidBy.getId());
        request.getParticipants().forEach(p -> requireMember(groupId, p.getUserId()));

        Category category = request.getCategoryId() == null ? null :
                categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));

        RecurringExpense recurring = RecurringExpense.builder()
                .group(group)
                .paidBy(paidBy)
                .createdBy(creator)
                .description(request.getDescription())
                .amount(request.getAmount())
                .splitType(request.getSplitType())
                .category(category)
                .frequency(request.getFrequency())
                .nextRunDate(request.getStartDate())
                .active(true)
                .build();

        request.getParticipants().forEach(p -> {
            User participant = userRepository.findById(p.getUserId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Participant not found: " + p.getUserId()));
            recurring.getParticipants().add(RecurringExpenseParticipant.builder()
                    .recurringExpense(recurring)
                    .user(participant)
                    .shareValue(p.getValue())
                    .build());
        });

        recurringExpenseRepository.save(recurring);
        return toResponse(recurring);
    }

    public List<RecurringExpenseResponse> listForGroup(Long groupId) {
        return recurringExpenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId)
                .stream().map(this::toResponse).toList();
    }

    public void setActive(Long id, boolean active) {
        RecurringExpense recurring = findOrThrow(id);
        recurring.setActive(active);
        recurringExpenseRepository.save(recurring);
    }

    public void delete(Long id) {
        recurringExpenseRepository.deleteById(id);
    }

    /** Manually generates this cycle's expense right now, without waiting for the scheduler.
     *  Exists mainly so the feature can actually be demoed/tested without waiting a month. */
    public ExpenseResponse runNow(Long id) {
        RecurringExpense recurring = findOrThrow(id);
        ExpenseResponse response = generateExpense(recurring);
        recurring.setNextRunDate(recurring.getFrequency().advance(recurring.getNextRunDate()));
        recurringExpenseRepository.save(recurring);
        return response;
    }

    /** Runs once a day; generates an expense for every active recurring template
     *  whose next run date has arrived, then advances it to the following cycle. */
    @Scheduled(cron = "0 0 6 * * *") // 6:00 AM server time, daily
    public void processDueRecurringExpenses() {
        LocalDate today = LocalDate.now();
        List<RecurringExpense> due = recurringExpenseRepository.findByActiveTrueAndNextRunDateLessThanEqual(today);

        for (RecurringExpense recurring : due) {
            generateExpense(recurring);
            recurring.setNextRunDate(recurring.getFrequency().advance(recurring.getNextRunDate()));
            recurringExpenseRepository.save(recurring);
        }
    }

    private ExpenseResponse generateExpense(RecurringExpense recurring) {
        ExpenseRequest request = new ExpenseRequest();
        request.setDescription(recurring.getDescription());
        request.setAmount(recurring.getAmount());
        request.setSplitType(recurring.getSplitType());
        request.setPaidBy(recurring.getPaidBy().getId());
        request.setCategoryId(recurring.getCategory() == null ? null : recurring.getCategory().getId());
        request.setParticipants(recurring.getParticipants().stream()
                .map(p -> {
                    ParticipantShareRequest share = new ParticipantShareRequest();
                    share.setUserId(p.getUser().getId());
                    share.setValue(p.getShareValue());
                    return share;
                })
                .toList());

        return expenseService.addExpense(recurring.getGroup().getId(), request);
    }

    private void requireMember(Long groupId, Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "User " + userId + " is not a member of this group");
        }
    }

    private RecurringExpense findOrThrow(Long id) {
        return recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Recurring expense not found"));
    }

    private RecurringExpenseResponse toResponse(RecurringExpense r) {
        return new RecurringExpenseResponse(
                r.getId(), r.getDescription(), r.getAmount(), r.getSplitType(),
                r.getPaidBy().getName(), r.getCategory() == null ? null : r.getCategory().getName(),
                r.getFrequency(), r.getNextRunDate(), r.isActive());
    }
}
