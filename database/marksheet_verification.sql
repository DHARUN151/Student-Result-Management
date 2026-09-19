CREATE TABLE IF NOT EXISTS enterprise.marksheet_verification (
    verification_id BIGSERIAL PRIMARY KEY,

    result_id INTEGER NOT NULL,

    verification_code VARCHAR(100) NOT NULL UNIQUE,

    document_hash VARCHAR(128),

    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    verified BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_marksheet_result
        FOREIGN KEY(result_id)
        REFERENCES enterprise.result(result_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_marksheet_verification_result
ON enterprise.marksheet_verification(result_id);

CREATE INDEX IF NOT EXISTS idx_marksheet_verification_code
ON enterprise.marksheet_verification(verification_code);