package com.splitter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddCommentRequest {
    @NotBlank
    @Size(max = 1000, message = "Comment is too long (max 1000 characters)")
    private String content;
}