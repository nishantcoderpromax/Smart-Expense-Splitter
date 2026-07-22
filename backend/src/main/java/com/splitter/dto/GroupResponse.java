package com.splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private String createdBy;
    private List<MemberResponse> members;
}

