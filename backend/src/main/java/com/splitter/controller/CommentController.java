package com.splitter.controller;

import com.splitter.dto.AddCommentRequest;
import com.splitter.dto.CommentResponse;
import com.splitter.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses/{expenseId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> add(@PathVariable Long expenseId, @Valid @RequestBody AddCommentRequest request) {
        return ResponseEntity.ok(commentService.add(expenseId, request));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> list(@PathVariable Long expenseId) {
        return ResponseEntity.ok(commentService.list(expenseId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long expenseId, @PathVariable Long commentId) {
        commentService.delete(expenseId, commentId);
        return ResponseEntity.noContent().build();
    }
}