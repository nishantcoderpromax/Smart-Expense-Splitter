CREATE TABLE groups (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(150) NOT NULL,
                        description VARCHAR(500),
                        created_by BIGINT NOT NULL REFERENCES users(id),
                        created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE group_members (
                               id BIGSERIAL PRIMARY KEY,
                               group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                               user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
                               joined_at TIMESTAMP NOT NULL DEFAULT now(),
                               UNIQUE (group_id, user_id)
);
