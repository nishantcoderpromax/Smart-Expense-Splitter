package com.splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSearchResult {
    private Long id;
    private String name;
    private String email;
}
