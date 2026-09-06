SET search_path TO enterprise;

-- MIGRATE OLD MARKS

INSERT INTO marks
(
    mark_id,
    student_id,
    offering_id,
    exam_id,
    internal_mark,
    external_mark,
    total_mark,
    grade,
    result_outcome,
    entered_by,
    version
)
SELECT
    m.mark_id,
    m.stud_id,

    CASE
        WHEN m.sub_id = 1 THEN 1
        WHEN m.sub_id = 8 THEN 2
        WHEN m.sub_id = 9 THEN 3
        WHEN m.sub_id = 3 THEN 4
        WHEN m.sub_id = 4 THEN 5
        WHEN m.sub_id = 7 THEN 6
    END,

    1,

    m.internal_mark,
    m.external_mark,
    m.total_mark,
    m.grade,

    CASE
        WHEN LOWER(m.status) = 'pass' THEN 'PASS'
        WHEN LOWER(m.status) = 'fail' THEN 'FAIL'
        ELSE 'UNKNOWN'
    END,

    1,

    1

FROM public.marks m;