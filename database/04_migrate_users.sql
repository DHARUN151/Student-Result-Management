SET search_path TO enterprise;

-- 1. CREATE FACULTY ACCOUNT FOR TEACHER

INSERT INTO faculty
(
    employee_code,
    name,
    status
)
VALUES
(
    'LEGACY-TEACHER-1',
    'teacher1',
    'ACTIVE'
);


-- 2. MIGRATE USERS

INSERT INTO users
(
    user_id,
    username,
    password_hash,
    account_status,
    student_id,
    faculty_id
)
SELECT
    u.user_id,
    u.username,
    u.password,
    'ACTIVE',
    u.stud_id,
    CASE
        WHEN u.role = 'TEACHER' THEN 1
        ELSE NULL
    END
FROM public.users u;

-- 3. ASSIGN FACULTY ROLE TO TEACHER

INSERT INTO user_roles
(
    user_id,
    role_id
)
VALUES
(
    1,
    4
);


-- 4. ASSIGN STUDENT ROLE

INSERT INTO user_roles
(
    user_id,
    role_id
)
VALUES
(
    2,
    6
);

INSERT INTO user_roles
(
    user_id,
    role_id
)
VALUES
(
    3,
    6
);

INSERT INTO user_roles(user_id, role_id) VALUES(4,6);

INSERT INTO user_roles (user_id,role_id) VALUES(5,6);