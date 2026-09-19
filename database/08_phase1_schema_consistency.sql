
-- PHASE 1: ENTERPRISE SCHEMA CONSISTENCY
-- Student Result Management System

CREATE SCHEMA IF NOT EXISTS enterprise;

-- ------------------------------------------------------------
-- USERS
-- ------------------------------------------------------------

ALTER TABLE enterprise.users
ADD COLUMN IF NOT EXISTS first_login BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE enterprise.users
ADD COLUMN IF NOT EXISTS two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE enterprise.users
ADD COLUMN IF NOT EXISTS two_factor_secret VARCHAR(255);

-- ------------------------------------------------------------
-- GENERATED ID SEQUENCES
-- ------------------------------------------------------------

CREATE SEQUENCE IF NOT EXISTS enterprise.users_user_id_seq;

CREATE SEQUENCE IF NOT EXISTS enterprise.students_student_id_seq;

CREATE SEQUENCE IF NOT EXISTS enterprise.student_address_address_id_seq;

CREATE SEQUENCE IF NOT EXISTS enterprise.subjects_subject_id_seq;

CREATE SEQUENCE IF NOT EXISTS enterprise.marks_mark_id_seq;

-- ------------------------------------------------------------
-- USERS ID
-- ------------------------------------------------------------

ALTER TABLE enterprise.users
ALTER COLUMN user_id SET DEFAULT nextval('enterprise.users_user_id_seq');

SELECT setval(
    'enterprise.users_user_id_seq',
    COALESCE((SELECT MAX(user_id) FROM enterprise.users), 0)
);

-- ------------------------------------------------------------
-- STUDENTS ID
-- ------------------------------------------------------------

ALTER TABLE enterprise.students
ALTER COLUMN student_id SET DEFAULT nextval('enterprise.students_student_id_seq');

SELECT setval(
    'enterprise.students_student_id_seq',
    COALESCE((SELECT MAX(student_id) FROM enterprise.students), 0)
);

-- ------------------------------------------------------------
-- STUDENT ADDRESS ID
-- ------------------------------------------------------------

ALTER TABLE enterprise.student_address
ALTER COLUMN address_id SET DEFAULT nextval('enterprise.student_address_address_id_seq');

SELECT setval(
    'enterprise.student_address_address_id_seq',
    COALESCE((SELECT MAX(address_id) FROM enterprise.student_address), 0)
);

-- ------------------------------------------------------------
-- SUBJECT ID
-- ------------------------------------------------------------

ALTER TABLE enterprise.subjects
ALTER COLUMN subject_id SET DEFAULT nextval('enterprise.subjects_subject_id_seq');

SELECT setval(
    'enterprise.subjects_subject_id_seq',
    COALESCE((SELECT MAX(subject_id) FROM enterprise.subjects), 0)
);

-- ------------------------------------------------------------
-- MARK ID
-- ------------------------------------------------------------

ALTER TABLE enterprise.marks
ALTER COLUMN mark_id SET DEFAULT nextval('enterprise.marks_mark_id_seq');

SELECT setval(
    'enterprise.marks_mark_id_seq',
    COALESCE((SELECT MAX(mark_id) FROM enterprise.marks), 0)
);

-- ------------------------------------------------------------
-- INDEXES
-- These will also help upcoming pagination/filter features.
-- ------------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_students_department
ON enterprise.students(department_id);

CREATE INDEX IF NOT EXISTS idx_students_year
ON enterprise.students(year);

CREATE INDEX IF NOT EXISTS idx_students_section
ON enterprise.students(section);

CREATE INDEX IF NOT EXISTS idx_marks_student
ON enterprise.marks(student_id);

CREATE INDEX IF NOT EXISTS idx_marks_subject
ON enterprise.marks(subject_id);

CREATE INDEX IF NOT EXISTS idx_users_role
ON enterprise.users(role_id);

CREATE INDEX IF NOT EXISTS idx_users_status
ON enterprise.users(account_status);