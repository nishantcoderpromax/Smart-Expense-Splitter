package com.splitter.service;

import com.splitter.dto.ActivityLogResponse;
import com.splitter.dto.PageResponse;
import com.splitter.entity.ActivityLog;
import com.splitter.entity.Group;
import com.splitter.entity.User;
import com.splitter.enums.ActivityType;
import com.splitter.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
 
@Service
@RequiredArgsConstructor
public class ActivityLogService {
 
    private final ActivityLogRepository activityLogRepository;
 
    /** Called by other services right after a mutation succeeds. */
    public void record(Group group, User actor, ActivityType type, String description) {
        activityLogRepository.save(ActivityLog.builder()
                .group(group)
                .actor(actor)
                .actionType(type)
                .description(description)
                .build());
    }
 
    public PageResponse<ActivityLogResponse> getTimeline(Long groupId, Pageable pageable) {
        return PageResponse.from(
                activityLogRepository.findByGroupIdOrderByCreatedAtDesc(groupId, pageable)
                        .map(log -> new ActivityLogResponse(
                                log.getId(), log.getActor().getName(), log.getActionType(),
                                log.getDescription(), log.getCreatedAt()))
        );
    }
}
