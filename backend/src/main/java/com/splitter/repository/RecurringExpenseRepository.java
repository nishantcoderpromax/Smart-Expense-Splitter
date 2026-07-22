package com.splitter.repository;

import com.splitter.entity.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {
    List<RecurringExpense> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    // picked up by the scheduler: anything active and due today or overdue
    List<RecurringExpense> findByActiveTrueAndNextRunDateLessThanEqual(LocalDate date);
}
