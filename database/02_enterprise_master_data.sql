SET search_path TO enterprise;


-- 1. ROLES


INSERT INTO roles (role_name, description)
VALUES
('SUPER_ADMIN', 'System administrator'),
('ADMIN', 'College administrator'),
('HOD', 'Head of Department'),
('FACULTY', 'Faculty member'),
('EXAM_CELL', 'Examination cell user'),
('STUDENT', 'Student user');



-- 2. PERMISSIONS


INSERT INTO permissions (permission_name, description)
VALUES
('STUDENT_VIEW', 'View student details'),
('STUDENT_CREATE', 'Add student'),
('STUDENT_UPDATE', 'Update student'),

('SUBJECT_VIEW', 'View subjects'),
('SUBJECT_CREATE', 'Add subject'),
('SUBJECT_UPDATE', 'Update subject'),

('MARKS_VIEW', 'View marks'),
('MARKS_CREATE', 'Enter marks'),
('MARKS_UPDATE', 'Update marks'),
('MARKS_SUBMIT', 'Submit marks'),

('RESULT_VIEW', 'View result'),
('RESULT_VERIFY', 'Verify result'),
('RESULT_APPROVE', 'Approve result'),
('RESULT_PUBLISH', 'Publish result'),

('USER_CREATE', 'Create user'),
('USER_UPDATE', 'Update user'),
('ROLE_ASSIGN', 'Assign role'),
('AUDIT_VIEW', 'View audit logs');



-- 3. DEPARTMENTS


INSERT INTO departments
(department_code, department_name, status)
VALUES
('ECE', 'Electronics and Communication Engineering', 'ACTIVE'),
('IT', 'Information Technology', 'ACTIVE');



-- 4. PROGRAMS


INSERT INTO programs
(department_id, program_code, program_name, degree_type, duration_years, status)
VALUES
(
    1,
    'BE-ECE',
    'B.E. Electronics and Communication Engineering',
    'B.E.',
    4,
    'ACTIVE'
);

INSERT INTO programs
(department_id, program_code, program_name, degree_type, duration_years, status)
VALUES
(
    2,
    'BTECH-IT',
    'B.Tech Information Technology',
    'B.Tech',
    4,
    'ACTIVE'
);



-- 5. ACADEMIC YEAR


INSERT INTO academic_years
(year_name, start_date, end_date, status)
VALUES
(
    '2026-2027',
    '2026-06-01',
    '2027-05-31',
    'ACTIVE'
);



-- 6. SEMESTER 7


INSERT INTO semesters
(program_id, academic_year_id, semester_number, status)
VALUES
(
    1,
    1,
    7,
    'ACTIVE'
);

INSERT INTO semesters
(program_id, academic_year_id, semester_number, status)
VALUES
(
    2,
    1,
    7,
    'ACTIVE'
);



-- 7. SUBJECTS


INSERT INTO subjects
(
    subject_id,
    subject_code,
    subject_name,
    credits,
    subject_type,
    max_internal_marks,
    max_external_marks,
    pass_marks,
    status
)
VALUES
(
    1,
    'EC107',
    'Wireless Communication',
    3,
    'THEORY',
    40,
    60,
    40,
    'ACTIVE'
);

INSERT INTO subjects
(
    subject_id,
    subject_code,
    subject_name,
    credits,
    subject_type,
    max_internal_marks,
    max_external_marks,
    pass_marks,
    status
)
VALUES
(
    3,
    'IT100',
    'Computer Networks',
    3,
    'THEORY',
    40,
    60,
    40,
    'ACTIVE'
);

INSERT INTO subjects
(
    subject_id,
    subject_code,
    subject_name,
    credits,
    subject_type,
    max_internal_marks,
    max_external_marks,
    pass_marks,
    status
)
VALUES
(
    4,
    'IT101',
    'OOPS',
    3,
    'THEORY',
    40,
    60,
    40,
    'ACTIVE'
);

INSERT INTO subjects
(
    subject_id,
    subject_code,
    subject_name,
    credits,
    subject_type,
    max_internal_marks,
    max_external_marks,
    pass_marks,
    status
)
VALUES
(
    7,
    'IT102',
    'Docker',
    3,
    'THEORY',
    40,
    60,
    40,
    'ACTIVE'
);

INSERT INTO subjects
(
    subject_id,
    subject_code,
    subject_name,
    credits,
    subject_type,
    max_internal_marks,
    max_external_marks,
    pass_marks,
    status
)
VALUES
(
    8,
    'EC110',
    'EVS',
    3,
    'THEORY',
    40,
    60,
    40,
    'ACTIVE'
);

INSERT INTO subjects
(
    subject_id,
    subject_code,
    subject_name,
    credits,
    subject_type,
    max_internal_marks,
    max_external_marks,
    pass_marks,
    status
)
VALUES
(
    9,
    'EC200',
    'JAVA',
    3,
    'THEORY',
    40,
    60,
    40,
    'ACTIVE'
);



-- 8. SUBJECT OFFERINGS


-- ECE subjects

INSERT INTO subject_offerings
(subject_id, semester_id, academic_year_id, status)
VALUES
(1, 1, 1, 'ACTIVE');

INSERT INTO subject_offerings
(subject_id, semester_id, academic_year_id, status)
VALUES
(8, 1, 1, 'ACTIVE');

INSERT INTO subject_offerings
(subject_id, semester_id, academic_year_id, status)
VALUES
(9, 1, 1, 'ACTIVE');


-- IT subjects

INSERT INTO subject_offerings
(subject_id, semester_id, academic_year_id, status)
VALUES
(3, 2, 1, 'ACTIVE');

INSERT INTO subject_offerings
(subject_id, semester_id, academic_year_id, status)
VALUES
(4, 2, 1, 'ACTIVE');

INSERT INTO subject_offerings
(subject_id, semester_id, academic_year_id, status)
VALUES
(7, 2, 1, 'ACTIVE');



-- 9. LEGACY EXAMINATION

INSERT INTO examinations
(
    academic_year_id,
    semester_id,
    exam_name,
    exam_type,
    status
)
VALUES
(
    1,
    1,
    'Legacy Imported Results',
    'LEGACY',
    'COMPLETED'
);