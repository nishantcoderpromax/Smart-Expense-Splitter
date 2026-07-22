CREATE TABLE expenses (
                          id BIGSERIAL PRIMARY KEY,
                          group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                          paid_by BIGINT NOT NULL REFERENCES users(id),
                          description VARCHAR(255) NOT NULL,
                          amount NUMERIC(12,2) NOT NULL,
                          split_type VARCHAR(20) NOT NULL,   -- EQUAL, UNEQUAL, PERCENTAGE, SHARES
                          created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- one row per participant per expense: how much of that expense they owe
CREATE TABLE expense_shares (
                                id BIGSERIAL PRIMARY KEY,
                                expense_id BIGINT NOT NULL REFERENCES expenses(id) ON DELETE CASCADE,
                                user_id BIGINT NOT NULL REFERENCES users(id),
                                share_value NUMERIC(12,4),      -- raw input: exact amount, percentage, or share count (null for EQUAL)
                                owed_amount NUMERIC(12,2) NOT NULL,  -- final computed amount this user owes
                                UNIQUE (expense_id, user_id)
);
