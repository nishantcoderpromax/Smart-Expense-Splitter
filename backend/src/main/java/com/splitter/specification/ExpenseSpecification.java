package com.splitter.specification;

import com.splitter.entity.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Each method returns a Specification that adds one WHERE condition.
 * When a filter isn't provided, we return a no-op "always true" predicate
 * (cb.conjunction()) rather than Java null — chaining .and(null) throws an
 * NPE in this Spring Data version instead of being silently skipped, so every
 * method here must always return a real, chainable Specification.
 */
public class ExpenseSpecification {

    private ExpenseSpecification() {}

    public static Specification<Expense> belongsToGroup(Long groupId) {
        return (root, query, cb) -> cb.equal(root.get("group").get("id"), groupId);
    }

    public static Specification<Expense> descriptionContains(String text) {
        if (text == null || text.isBlank()) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.like(cb.lower(root.get("description")), "%" + text.toLowerCase() + "%");
    }

    public static Specification<Expense> hasCategory(Long categoryId) {
        if (categoryId == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Expense> paidByUser(Long userId) {
        if (userId == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("paidBy").get("id"), userId);
    }

    public static Specification<Expense> amountAtLeast(BigDecimal min) {
        if (min == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<Expense> amountAtMost(BigDecimal max) {
        if (max == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), max);
    }
}
