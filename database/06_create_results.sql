SET search_path TO enterprise;

-- CREATE RESULTS FROM EXISTING MARKS

INSERT INTO result
(
    student_id,
    exam_id,
    semester_id,
    workflow_status,
    total_marks,
    maximum_marks,
    percentage,
    result_class
)
SELECT
    m.student_id,
    m.exam_id,
    e.semester_id,

    'PUBLISHED',

    SUM(m.total_mark),

    COUNT(m.mark_id) * 100,

    ROUND(
        (SUM(m.total_mark)::NUMERIC /
        (COUNT(m.mark_id) * 100)) * 100,
        2
    ),

    CASE
        WHEN MIN(m.result_outcome) = 'FAIL'
            THEN 'FAIL'
        ELSE 'PASS'
    END

FROM marks m

JOIN examinations e
    ON e.exam_id = m.exam_id

GROUP BY
    m.student_id,
    m.exam_id,
    e.semester_id;