package com.splitter.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name; // null/blank = leave unchanged

    // both required together to change password; left blank to skip
    private String currentPassword;

    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;
}
