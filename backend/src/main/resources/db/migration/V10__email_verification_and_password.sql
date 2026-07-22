-- DEFAULT true so existing accounts aren't suddenly locked out of anything;
-- new registrations explicitly set this to false in application code.
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT true;
 
-- One shared table for both email-verification and password-reset tokens,
-- distinguished by "purpose" — avoids duplicating near-identical tables.
CREATE TABLE verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    purpose VARCHAR(30) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);