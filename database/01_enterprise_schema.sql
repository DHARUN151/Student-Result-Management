CREATE SCHEMA IF NOT EXISTS enterprise;

SET search_path TO enterprise;

CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    permission_id SERIAL PRIMARY KEY,
    permission_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE departments (
    department_id SERIAL PRIMARY KEY,
    department_code VARCHAR(20) NOT NULL UNIQUE,
    department_name VARCHAR(150) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE programs (
    program_id SERIAL PRIMARY KEY,
    department_id INTEGER NOT NULL,
    program_code VARCHAR(30) NOT NULL UNIQUE,
    program_name VARCHAR(150) NOT NULL,
    degree_type VARCHAR(30),
    duration_years INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_program_department
        FOREIGN KEY (department_id)
        REFERENCES departments(department_id),

    CONSTRAINT chk_program_duration
        CHECK (duration_years IS NULL OR duration_years > 0)
);

CREATE TABLE academic_years (
    academic_year_id SERIAL PRIMARY KEY,
    year_name VARCHAR(20) NOT NULL UNIQUE,
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE semesters (
    semester_id SERIAL PRIMARY KEY,
    program_id INTEGER NOT NULL,
    academic_year_id INTEGER NOT NULL,
    semester_number INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_semester_program
        FOREIGN KEY (program_id)
        REFERENCES programs(program_id),

    CONSTRAINT fk_semester_academic_year
        FOREIGN KEY (academic_year_id)
        REFERENCES academic_years(academic_year_id),

    CONSTRAINT chk_semester_number
        CHECK (semester_number BETWEEN 1 AND 12),

    CONSTRAINT uq_program_year_semester
        UNIQUE (program_id, academic_year_id, semester_number)
);

CREATE TABLE students (
    student_id INTEGER PRIMARY KEY,
    reg_num VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    dob DATE NOT NULL,
    gender VARCHAR(10),
    phone VARCHAR(15),
    email VARCHAR(100),
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE student_address (
    address_id INTEGER PRIMARY KEY,
    student_id INTEGER NOT NULL,
    door_no VARCHAR(20),
    street VARCHAR(100),
    city VARCHAR(50),
    district VARCHAR(50),
    state VARCHAR(50),
    pincode VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_address_student
        FOREIGN KEY (student_id)
        REFERENCES students(student_id)
);

CREATE TABLE academic_details (
    academic_detail_id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL UNIQUE,
    program_id INTEGER,
    admission_year INTEGER,
    current_semester_id INTEGER,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_academic_student
        FOREIGN KEY (student_id)
        REFERENCES students(student_id),

    CONSTRAINT fk_academic_program
        FOREIGN KEY (program_id)
        REFERENCES programs(program_id),

    CONSTRAINT fk_academic_semester
        FOREIGN KEY (current_semester_id)
        REFERENCES semesters(semester_id)
);

CREATE TABLE subjects (
    subject_id INTEGER PRIMARY KEY,
    subject_code VARCHAR(20) NOT NULL UNIQUE,
    subject_name VARCHAR(100) NOT NULL,
    credits INTEGER,
    subject_type VARCHAR(30) NOT NULL DEFAULT 'THEORY',
    max_internal_marks INTEGER NOT NULL DEFAULT 40,
    max_external_marks INTEGER NOT NULL DEFAULT 60,
    pass_marks INTEGER NOT NULL DEFAULT 40,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_subject_credits
        CHECK (credits IS NULL OR credits > 0),

    CONSTRAINT chk_internal_marks
        CHECK (max_internal_marks >= 0),

    CONSTRAINT chk_external_marks
        CHECK (max_external_marks >= 0),

    CONSTRAINT chk_pass_marks
        CHECK (pass_marks >= 0)
);

CREATE TABLE subject_offerings (
    offering_id SERIAL PRIMARY KEY,
    subject_id INTEGER NOT NULL,
    semester_id INTEGER NOT NULL,
    academic_year_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_offering_subject
        FOREIGN KEY (subject_id)
        REFERENCES subjects(subject_id),

    CONSTRAINT fk_offering_semester
        FOREIGN KEY (semester_id)
        REFERENCES semesters(semester_id),

    CONSTRAINT fk_offering_academic_year
        FOREIGN KEY (academic_year_id)
        REFERENCES academic_years(academic_year_id),

    CONSTRAINT uq_subject_offering
        UNIQUE (subject_id, semester_id, academic_year_id)
);

CREATE TABLE faculty (
    faculty_id SERIAL PRIMARY KEY,
    employee_code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(15),
    department_id INTEGER,
    designation VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_faculty_department
        FOREIGN KEY (department_id)
        REFERENCES departments(department_id)
);

CREATE TABLE faculty_subject (
    assignment_id SERIAL PRIMARY KEY,
    faculty_id INTEGER NOT NULL,
    offering_id INTEGER NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_faculty_assignment
        FOREIGN KEY (faculty_id)
        REFERENCES faculty(faculty_id),

    CONSTRAINT fk_subject_assignment
        FOREIGN KEY (offering_id)
        REFERENCES subject_offerings(offering_id),

    CONSTRAINT uq_faculty_offering
        UNIQUE (faculty_id, offering_id)
);

CREATE TABLE examinations (
    exam_id SERIAL PRIMARY KEY,
    academic_year_id INTEGER NOT NULL,
    semester_id INTEGER NOT NULL,
    exam_name VARCHAR(150) NOT NULL,
    exam_type VARCHAR(30) NOT NULL,
    start_date DATE,
    end_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_exam_academic_year
        FOREIGN KEY (academic_year_id)
        REFERENCES academic_years(academic_year_id),

    CONSTRAINT fk_exam_semester
        FOREIGN KEY (semester_id)
        REFERENCES semesters(semester_id)
);

CREATE TABLE users (
    user_id INTEGER PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    student_id INTEGER,
    faculty_id INTEGER,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_student
        FOREIGN KEY (student_id)
        REFERENCES students(student_id),

    CONSTRAINT fk_user_faculty
        FOREIGN KEY (faculty_id)
        REFERENCES faculty(faculty_id),

    CONSTRAINT chk_failed_attempts
        CHECK (failed_login_attempts >= 0)
);

CREATE TABLE user_roles (
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id)
        REFERENCES roles(role_id)
        ON DELETE CASCADE
);

CREATE TABLE role_permissions (
    role_id INTEGER NOT NULL,
    permission_id INTEGER NOT NULL,

    PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permission_role
        FOREIGN KEY (role_id)
        REFERENCES roles(role_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_role_permission_permission
        FOREIGN KEY (permission_id)
        REFERENCES permissions(permission_id)
        ON DELETE CASCADE
);

CREATE TABLE marks (
    mark_id INTEGER PRIMARY KEY,
    student_id INTEGER NOT NULL,
    offering_id INTEGER NOT NULL,
    exam_id INTEGER NOT NULL,
    internal_mark INTEGER NOT NULL,
    external_mark INTEGER NOT NULL,
    total_mark INTEGER NOT NULL,
    grade VARCHAR(5),
    result_outcome VARCHAR(20),
    entered_by INTEGER,
    entered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by INTEGER,
    updated_at TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT fk_mark_student
        FOREIGN KEY (student_id)
        REFERENCES students(student_id),

    CONSTRAINT fk_mark_offering
        FOREIGN KEY (offering_id)
        REFERENCES subject_offerings(offering_id),

    CONSTRAINT fk_mark_exam
        FOREIGN KEY (exam_id)
        REFERENCES examinations(exam_id),

    CONSTRAINT fk_mark_entered_by
        FOREIGN KEY (entered_by)
        REFERENCES users(user_id),

    CONSTRAINT fk_mark_updated_by
        FOREIGN KEY (updated_by)
        REFERENCES users(user_id),

    CONSTRAINT uq_student_offering_exam
        UNIQUE (student_id, offering_id, exam_id),

    CONSTRAINT chk_internal_mark
        CHECK (internal_mark >= 0),

    CONSTRAINT chk_external_mark
        CHECK (external_mark >= 0),

    CONSTRAINT chk_total_mark
        CHECK (total_mark >= 0),

    CONSTRAINT chk_mark_version
        CHECK (version > 0)
);

CREATE TABLE result (
    result_id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL,
    exam_id INTEGER NOT NULL,
    semester_id INTEGER NOT NULL,
    workflow_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    total_marks INTEGER,
    maximum_marks INTEGER,
    percentage NUMERIC(5,2),
    cgpa NUMERIC(4,2),
    result_class VARCHAR(50),
    processed_at TIMESTAMP,
    processed_by INTEGER,
    published_at TIMESTAMP,
    published_by INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_result_student
        FOREIGN KEY (student_id)
        REFERENCES students(student_id),

    CONSTRAINT fk_result_exam
        FOREIGN KEY (exam_id)
        REFERENCES examinations(exam_id),

    CONSTRAINT fk_result_semester
        FOREIGN KEY (semester_id)
        REFERENCES semesters(semester_id),

    CONSTRAINT fk_result_processed_by
        FOREIGN KEY (processed_by)
        REFERENCES users(user_id),

    CONSTRAINT fk_result_published_by
        FOREIGN KEY (published_by)
        REFERENCES users(user_id),

    CONSTRAINT uq_student_exam_result
        UNIQUE (student_id, exam_id),

    CONSTRAINT chk_result_percentage
        CHECK (percentage IS NULL OR (percentage >= 0 AND percentage <= 100)),

    CONSTRAINT chk_result_cgpa
        CHECK (cgpa IS NULL OR (cgpa >= 0 AND cgpa <= 10))
);

CREATE TABLE result_approval (
    approval_id SERIAL PRIMARY KEY,
    result_id INTEGER NOT NULL,
    action VARCHAR(40) NOT NULL,
    performed_by INTEGER NOT NULL,
    remarks VARCHAR(500),
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_approval_result
        FOREIGN KEY (result_id)
        REFERENCES result(result_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_approval_user
        FOREIGN KEY (performed_by)
        REFERENCES users(user_id)
);

CREATE TABLE audit_logs (
    audit_id BIGSERIAL PRIMARY KEY,
    user_id INTEGER,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    remarks TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
);

CREATE INDEX idx_students_reg_num
    ON students(reg_num);

CREATE INDEX idx_students_email
    ON students(email);

CREATE INDEX idx_academic_program
    ON academic_details(program_id);

CREATE INDEX idx_academic_semester
    ON academic_details(current_semester_id);

CREATE INDEX idx_subject_offering_semester
    ON subject_offerings(semester_id);

CREATE INDEX idx_faculty_department
    ON faculty(department_id);

CREATE INDEX idx_marks_student
    ON marks(student_id);

CREATE INDEX idx_marks_exam
    ON marks(exam_id);

CREATE INDEX idx_result_student
    ON result(student_id);

CREATE INDEX idx_result_status
    ON result(workflow_status);

CREATE INDEX idx_audit_user
    ON audit_logs(user_id);

CREATE INDEX idx_audit_entity
    ON audit_logs(entity_type, entity_id);