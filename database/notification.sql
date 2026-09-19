CREATE TABLE IF NOT EXISTS enterprise.notifications (
    notification_id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL,
    result_id INTEGER,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    result_link VARCHAR(500),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    email_sent BOOLEAN NOT NULL DEFAULT FALSE,
    email_sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_student
        FOREIGN KEY (student_id)
        REFERENCES enterprise.students(student_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_notification_result
        FOREIGN KEY (result_id)
        REFERENCES enterprise.result(result_id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_notifications_student
ON enterprise.notifications(student_id);

CREATE INDEX IF NOT EXISTS idx_notifications_unread
ON enterprise.notifications(student_id, is_read);

CREATE INDEX IF NOT EXISTS idx_notifications_result
ON enterprise.notifications(result_id);