package com.splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String content;
    private LocalDateTime createdAt;
}
