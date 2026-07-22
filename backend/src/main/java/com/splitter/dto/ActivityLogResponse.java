package com.splitter.dto;

import com.splitter.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ActivityLogResponse {
    private Long id;
    private String actorName;
    private ActivityType type;
    private String description;
    private LocalDateTime createdAt; // frontend computes "2 min ago" itself, recalculated live
}