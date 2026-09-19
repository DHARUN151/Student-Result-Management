CREATE TABLE IF NOT EXISTS enterprise.digilocker_documents (
    digilocker_id BIGSERIAL PRIMARY KEY,

    result_id INTEGER NOT NULL UNIQUE,

    student_id INTEGER NOT NULL,

    semester INTEGER NOT NULL,

    document_type VARCHAR(50) NOT NULL
        DEFAULT 'MARKSHEET',

    document_status VARCHAR(30) NOT NULL
        DEFAULT 'READY',

    verification_code VARCHAR(100),

    prepared_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    published_at TIMESTAMP,

    remarks VARCHAR(500),

    CONSTRAINT fk_digilocker_result
        FOREIGN KEY(result_id)
        REFERENCES enterprise.result(result_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_digilocker_student
        FOREIGN KEY(student_id)
        REFERENCES enterprise.students(student_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_digilocker_student
ON enterprise.digilocker_documents(student_id);

CREATE INDEX IF NOT EXISTS idx_digilocker_status
ON enterprise.digilocker_documents(document_status);

CREATE INDEX IF NOT EXISTS idx_digilocker_result
ON enterprise.digilocker_documents(result_id);