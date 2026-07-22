package com.splitter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddMemberRequest {
    @NotBlank @Email
    private String email; // add an existing user to the group by email
}
