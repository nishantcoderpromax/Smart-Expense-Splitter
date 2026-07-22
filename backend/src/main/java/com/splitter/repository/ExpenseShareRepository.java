package com.splitter.repository;

import com.splitter.entity.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {
    List<ExpenseShare> findByExpenseGroupId(Long groupId);
}

