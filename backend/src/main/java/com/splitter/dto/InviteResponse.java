package com.splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class InviteResponse {
    private String token;
    private String inviteUrl; // full shareable link, ready to copy/paste
    private LocalDateTime expiresAt;
}
