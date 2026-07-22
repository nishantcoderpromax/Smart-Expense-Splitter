CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE refresh_tokens (
                                id BIGSERIAL PRIMARY KEY,
                                token VARCHAR(255) NOT NULL UNIQUE,
                                user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                expiry_date TIMESTAMP NOT NULL,
                                created_at TIMESTAMP NOT NULL DEFAULT now()
);
