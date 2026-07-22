package com.splitter.enums;

public enum SplitType {
    EQUAL,       // amount divided evenly among participants
    UNEQUAL,     // each participant's exact owed amount is given directly
    PERCENTAGE,  // each participant owes a % of the total (must sum to 100)
    SHARES       // each participant has N shares (e.g. 2:1:1), owed amount proportional to shares
}
