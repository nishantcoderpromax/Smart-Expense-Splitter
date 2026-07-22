CREATE TABLE activity_log (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    actor_id BIGINT NOT NULL REFERENCES users(id),
    action_type VARCHAR(30) NOT NULL,
    description VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_log_group_created ON activity_log (group_id, created_at DESC);