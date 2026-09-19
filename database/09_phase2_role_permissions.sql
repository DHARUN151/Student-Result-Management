SET search_path TO enterprise;

INSERT INTO permissions (permission_name,description)
VALUES
('USER_VIEW','View user accounts'),
('USER_CREATE','Create user accounts'),
('USER_UPDATE','Update user accounts'),
('USER_DEACTIVATE','Activate or deactivate user accounts'),
('STUDENT_VIEW','View student details'),
('STUDENT_CREATE','Add student'),
('STUDENT_UPDATE','Update student'),
('STUDENT_IMPORT','Import students using CSV'),
('SUBJECT_VIEW','View subjects'),
('SUBJECT_CREATE','Add subject'),
('SUBJECT_UPDATE','Update subject'),
('MARKS_VIEW','View marks'),
('MARKS_CREATE','Enter marks'),
('MARKS_UPDATE','Update marks'),
('MARKS_SUBMIT','Submit marks'),
('RESULT_VIEW','View result'),
('RESULT_VERIFY','Verify result'),
('RESULT_APPROVE','Approve result'),
('RESULT_PUBLISH','Publish result'),
('ROLE_ASSIGN','Assign roles'),
('AUDIT_VIEW','View audit logs')
ON CONFLICT(permission_name) DO NOTHING;

INSERT INTO role_permissions(role_id,permission_id)
SELECT r.role_id,p.permission_id
FROM roles r
CROSS JOIN permissions p
WHERE r.role_name IN ('SUPER_ADMIN','ADMIN')
ON CONFLICT(role_id,permission_id) DO NOTHING;

INSERT INTO role_permissions(role_id,permission_id)
SELECT r.role_id,p.permission_id
FROM roles r
JOIN permissions p ON p.permission_name IN (
'STUDENT_VIEW',
'STUDENT_UPDATE',
'SUBJECT_VIEW',
'MARKS_VIEW',
'MARKS_CREATE',
'MARKS_UPDATE',
'MARKS_SUBMIT',
'RESULT_VIEW'
)
WHERE r.role_name='FACULTY'
ON CONFLICT(role_id,permission_id) DO NOTHING;

INSERT INTO role_permissions(role_id,permission_id)
SELECT r.role_id,p.permission_id
FROM roles r
JOIN permissions p ON p.permission_name IN (
'STUDENT_VIEW',
'SUBJECT_VIEW',
'MARKS_VIEW',
'RESULT_VIEW',
'RESULT_VERIFY'
)
WHERE r.role_name='HOD'
ON CONFLICT(role_id,permission_id) DO NOTHING;

INSERT INTO role_permissions(role_id,permission_id)
SELECT r.role_id,p.permission_id
FROM roles r
JOIN permissions p ON p.permission_name IN (
'STUDENT_VIEW',
'SUBJECT_VIEW',
'MARKS_VIEW',
'RESULT_VIEW',
'RESULT_APPROVE',
'RESULT_PUBLISH'
)
WHERE r.role_name='EXAM_CELL'
ON CONFLICT(role_id,permission_id) DO NOTHING;

INSERT INTO role_permissions(role_id,permission_id)
SELECT r.role_id,p.permission_id
FROM roles r
JOIN permissions p ON p.permission_name IN (
'STUDENT_VIEW',
'RESULT_VIEW'
)
WHERE r.role_name='STUDENT'
ON CONFLICT(role_id,permission_id) DO NOTHING;

INSERT INTO role_permissions(role_id,permission_id)
SELECT r.role_id,p.permission_id
FROM roles r
JOIN permissions p ON p.permission_name='STUDENT_IMPORT'
WHERE r.role_name='FACULTY'
ON CONFLICT(role_id,permission_id) DO NOTHING;