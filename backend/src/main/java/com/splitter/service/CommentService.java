package com.splitter.service;

import com.splitter.dto.AddCommentRequest;
import com.splitter.dto.CommentResponse;
import com.splitter.entity.Expense;
import com.splitter.entity.ExpenseComment;
import com.splitter.entity.User;
import com.splitter.exception.ApiException;
import com.splitter.repository.ExpenseCommentRepository;
import com.splitter.repository.ExpenseRepository;
import com.splitter.repository.GroupMemberRepository;
import com.splitter.repository.UserRepository;
import com.splitter.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final ExpenseCommentRepository commentRepository;
    private final ExpenseRepository expenseRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupEventPublisher eventPublisher;

    public CommentResponse add(Long expenseId, AddCommentRequest request) {
        Expense expense = findExpenseOrThrow(expenseId);
        User author = currentUser();
        requireMember(expense.getGroup().getId(), author.getId());

        ExpenseComment comment = ExpenseComment.builder()
                .expense(expense)
                .user(author)
                .content(request.getContent())
                .build();
        commentRepository.save(comment);

        eventPublisher.publishExpenseEvent(expenseId, "COMMENT_ADDED");
        return toResponse(comment);
    }

    public List<CommentResponse> list(Long expenseId) {
        Expense expense = findExpenseOrThrow(expenseId);
        requireMember(expense.getGroup().getId(), currentUser().getId());

        return commentRepository.findByExpenseIdOrderByCreatedAtAsc(expenseId)
                .stream().map(this::toResponse).toList();
    }

    public void delete(Long expenseId, Long commentId) {
        ExpenseComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Comment not found"));

        User actor = currentUser();
        // only the comment's own author can delete it — keeps the rule simple and predictable
        if (!comment.getUser().getId().equals(actor.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only delete your own comments");
        }

        commentRepository.delete(comment);
        eventPublisher.publishExpenseEvent(expenseId, "COMMENT_DELETED");
    }

    private Expense findExpenseOrThrow(Long expenseId) {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Expense not found"));
    }

    private void requireMember(Long groupId, Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not a member of this group");
        }
    }

    private User currentUser() {
        return userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private CommentResponse toResponse(ExpenseComment c) {
        return new CommentResponse(c.getId(), c.getUser().getId(), c.getUser().getName(), c.getContent(), c.getCreatedAt());
    }
}

