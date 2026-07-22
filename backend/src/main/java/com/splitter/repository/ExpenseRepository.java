package com.splitter.repository;

import com.splitter.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    List<Expense> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    @org.springframework.data.jpa.repository.Query(
            "SELECT e FROM Expense e WHERE e.group.id IN " +
                    "(SELECT gm.group.id FROM GroupMember gm WHERE gm.user.id = :userId) " +
                    "ORDER BY e.createdAt DESC")
    List<Expense> findRecentForUser(@org.springframework.data.repository.query.Param("userId") Long userId,
                                    org.springframework.data.domain.Pageable pageable);
}
