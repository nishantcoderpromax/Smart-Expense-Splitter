CREATE TABLE settlements (
                             id BIGSERIAL PRIMARY KEY,
                             group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                             from_user_id BIGINT NOT NULL REFERENCES users(id),  -- who paid
                             to_user_id BIGINT NOT NULL REFERENCES users(id),    -- who received payment
                             amount NUMERIC(12,2) NOT NULL,
                             settled_at TIMESTAMP NOT NULL DEFAULT now()
);
