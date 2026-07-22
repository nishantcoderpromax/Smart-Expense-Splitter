package com.splitter.repository;

import com.splitter.entity.ExpenseComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseCommentRepository extends JpaRepository<ExpenseComment, Long> {
    List<ExpenseComment> findByExpenseIdOrderByCreatedAtAsc(Long expenseId);
}

