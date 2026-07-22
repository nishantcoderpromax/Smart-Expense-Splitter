package com.splitter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ParticipantShareRequest {
    @NotNull
    private Long userId;

    // Meaning depends on splitType:
    // EQUAL -> ignored (send null)
    // UNEQUAL -> exact amount this user owes
    // PERCENTAGE -> percentage (0-100) this user owes
    // SHARES -> number of shares this user holds (e.g. 1, 2, 3)
    private BigDecimal value;
}

