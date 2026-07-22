package com.splitter.service;


import com.splitter.dto.RecordSettlementRequest;
import com.splitter.dto.SettlementHistoryResponse;
import com.splitter.entity.Group;
import com.splitter.entity.Settlement;
import com.splitter.entity.User;
import com.splitter.enums.ActivityType;
import com.splitter.exception.ApiException;
import com.splitter.repository.GroupMemberRepository;
import com.splitter.repository.GroupRepository;
import com.splitter.repository.SettlementRepository;
import com.splitter.repository.UserRepository;
import com.splitter.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
 
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class SettlementService {
 
    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupEventPublisher eventPublisher;
    private final ActivityLogService activityLogService;
 
    /** The logged-in user is always the one who paid (fromUser). */
    public SettlementHistoryResponse record(Long groupId, RecordSettlementRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Group not found"));
 
        User fromUser = userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
 
        User toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Recipient not found"));
 
        if (fromUser.getId().equals(toUser.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot settle up with yourself");
        }
        requireMember(groupId, fromUser.getId());
        requireMember(groupId, toUser.getId());
 
        Settlement settlement = Settlement.builder()
                .group(group)
                .fromUser(fromUser)
                .toUser(toUser)
                .amount(request.getAmount())
                .build();
        settlementRepository.save(settlement);
 
        activityLogService.record(group, fromUser, ActivityType.SETTLEMENT_RECORDED,
                String.format("%s paid %s %s", fromUser.getName(), toUser.getName(), request.getAmount()));
        eventPublisher.publish(groupId, "SETTLEMENT_RECORDED");
 
        return toResponse(settlement);
    }
 
    public List<SettlementHistoryResponse> history(Long groupId) {
        return settlementRepository.findByGroupIdOrderBySettledAtDesc(groupId)
                .stream().map(this::toResponse).toList();
    }
 
    private void requireMember(Long groupId, Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "User " + userId + " is not a member of this group");
        }
    }
 
    private SettlementHistoryResponse toResponse(Settlement s) {
        return new SettlementHistoryResponse(
                s.getId(), s.getFromUser().getName(), s.getToUser().getName(), s.getAmount(), s.getSettledAt());
    }
}