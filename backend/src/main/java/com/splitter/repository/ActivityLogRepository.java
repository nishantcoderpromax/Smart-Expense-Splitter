package com.splitter.repository;

import com.splitter.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Page<ActivityLog> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);
}

