package com.splitter.enums;

import java.time.LocalDate;

public enum RecurrenceFrequency {
    WEEKLY {
        public LocalDate advance(LocalDate date) { return date.plusWeeks(1); }
    },
    MONTHLY {
        public LocalDate advance(LocalDate date) { return date.plusMonths(1); }
    },
    YEARLY {
        public LocalDate advance(LocalDate date) { return date.plusYears(1); }
    };

    public abstract LocalDate advance(LocalDate date);
}