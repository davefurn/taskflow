CREATE TABLE password_reset_tokens (
                                       id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                       user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       token      VARCHAR(255) NOT NULL UNIQUE,
                                       type       VARCHAR(30)  NOT NULL CHECK (type IN ('email_verification', 'password_reset')),
                                       expires_at TIMESTAMPTZ  NOT NULL,
                                       created_at TIMESTAMPTZ  DEFAULT NOW()
);

CREATE INDEX idx_prt_token ON password_reset_tokens(token);
CREATE INDEX idx_prt_user  ON password_reset_tokens(user_id);