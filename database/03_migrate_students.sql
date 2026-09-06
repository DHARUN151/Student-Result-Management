SET search_path TO enterprise;

-- 1. MIGRATE STUDENTS

INSERT INTO students
(
    student_id,
    reg_num,
    name,
    dob,
    gender,
    phone,
    email,
    account_status
)
SELECT
    stud_id,
    reg_num,
    name,
    dob,
    gender,
    phone,
    email,
    'ACTIVE'
FROM public.students;


-- 2. MIGRATE STUDENT ADDRESSES

INSERT INTO student_address
(
    address_id,
    student_id,
    door_no,
    street,
    city,
    district,
    state,
    pincode
)
SELECT
    add_id,
    stud_id,
    door_no,
    street,
    city,
    district,
    state,
    pincode
FROM public.student_address;


-- 3. MIGRATE ACADEMIC DETAILS

INSERT INTO academic_details
(
    student_id,
    program_id,
    admission_year,
    current_semester_id,
    status
)
SELECT
    ad.stud_id,

    CASE
        WHEN ad.depart = 'ECE' THEN 1
        WHEN ad.depart = 'IT' THEN 2
        ELSE NULL
    END,

    ad.admission_year,

    CASE
        WHEN ad.depart = 'ECE' THEN 1
        WHEN ad.depart = 'IT' THEN 2
        ELSE NULL
    END,

    'ACTIVE'

FROM public.academic_details ad;