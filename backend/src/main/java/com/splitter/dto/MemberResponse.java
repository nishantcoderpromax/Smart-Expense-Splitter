package com.splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberResponse {
    private Long userId;
    private String name;
    private String email;
    private String role;
}

