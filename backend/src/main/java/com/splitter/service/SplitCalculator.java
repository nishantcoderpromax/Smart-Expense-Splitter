package com.splitter.service;

import com.splitter.dto.ParticipantShareRequest;
import com.splitter.enums.SplitType;
import com.splitter.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns (total amount, split type, participant inputs) into
 * a userId -> owedAmount map that always sums exactly to the total.
 */
@Component
public class SplitCalculator {

    public Map<Long, BigDecimal> calculate(BigDecimal amount, SplitType type, List<ParticipantShareRequest> participants) {
        return switch (type) {
            case EQUAL -> splitEqual(amount, participants);
            case UNEQUAL -> splitUnequal(amount, participants);
            case PERCENTAGE -> splitByPercentage(amount, participants);
            case SHARES -> splitByShares(amount, participants);
        };
    }

    private Map<Long, BigDecimal> splitEqual(BigDecimal amount, List<ParticipantShareRequest> participants) {
        int n = participants.size();
        BigDecimal base = amount.divide(BigDecimal.valueOf(n), 2, RoundingMode.FLOOR);
        BigDecimal remainder = amount.subtract(base.multiply(BigDecimal.valueOf(n)));

        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            // distribute leftover paise/cents one at a time so the total matches exactly
            BigDecimal share = (i < remainder.movePointRight(2).intValue()) ? base.add(new BigDecimal("0.01")) : base;
            result.put(participants.get(i).getUserId(), share);
        }
        return result;
    }

    private Map<Long, BigDecimal> splitUnequal(BigDecimal amount, List<ParticipantShareRequest> participants) {
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (ParticipantShareRequest p : participants) {
            requireValue(p, "an exact amount");
            result.put(p.getUserId(), p.getValue());
            sum = sum.add(p.getValue());
        }
        if (sum.compareTo(amount) != 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "UNEQUAL shares must add up to the total amount (" + amount + "), got " + sum);
        }
        return result;
    }

    private Map<Long, BigDecimal> splitByPercentage(BigDecimal amount, List<ParticipantShareRequest> participants) {
        BigDecimal totalPercent = BigDecimal.ZERO;
        for (ParticipantShareRequest p : participants) {
            requireValue(p, "a percentage");
            totalPercent = totalPercent.add(p.getValue());
        }
        if (totalPercent.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Percentages must add up to 100, got " + totalPercent);
        }

        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal runningTotal = BigDecimal.ZERO;
        for (int i = 0; i < participants.size(); i++) {
            ParticipantShareRequest p = participants.get(i);
            BigDecimal share;
            if (i == participants.size() - 1) {
                // last participant absorbs the rounding remainder so the sum is exact
                share = amount.subtract(runningTotal).setScale(2, RoundingMode.HALF_UP);
            } else {
                share = amount.multiply(p.getValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                runningTotal = runningTotal.add(share);
            }
            result.put(p.getUserId(), share);
        }
        return result;
    }

    private Map<Long, BigDecimal> splitByShares(BigDecimal amount, List<ParticipantShareRequest> participants) {
        BigDecimal totalShares = BigDecimal.ZERO;
        for (ParticipantShareRequest p : participants) {
            requireValue(p, "a share count");
            totalShares = totalShares.add(p.getValue());
        }
        if (totalShares.signum() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Total shares must be greater than 0");
        }

        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal runningTotal = BigDecimal.ZERO;
        for (int i = 0; i < participants.size(); i++) {
            ParticipantShareRequest p = participants.get(i);
            BigDecimal share;
            if (i == participants.size() - 1) {
                share = amount.subtract(runningTotal).setScale(2, RoundingMode.HALF_UP);
            } else {
                share = amount.multiply(p.getValue())
                        .divide(totalShares, 2, RoundingMode.HALF_UP);
                runningTotal = runningTotal.add(share);
            }
            result.put(p.getUserId(), share);
        }
        return result;
    }

    private void requireValue(ParticipantShareRequest p, String what) {
        if (p.getValue() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Participant " + p.getUserId() + " is missing " + what);
        }
    }
}
