CREATE TABLE recurring_expenses (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    paid_by BIGINT NOT NULL REFERENCES users(id),
    created_by BIGINT NOT NULL REFERENCES users(id),
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    split_type VARCHAR(20) NOT NULL,
    category_id BIGINT REFERENCES categories(id),
    frequency VARCHAR(20) NOT NULL,   -- WEEKLY, MONTHLY, YEARLY
    next_run_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Stores the same per-participant split config an expense needs (see expense_shares),
-- so the scheduler can regenerate an identical expense every cycle without asking again.
CREATE TABLE recurring_expense_participants (
    id BIGSERIAL PRIMARY KEY,
    recurring_expense_id BIGINT NOT NULL REFERENCES recurring_expenses(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    share_value NUMERIC(12,4),
    UNIQUE (recurring_expense_id, user_id)
);