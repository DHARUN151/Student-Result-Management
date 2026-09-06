SET search_path TO enterprise;

-- ADD MIGRATION AUDIT FOR OLD RESULTS

INSERT INTO result_approval
(
    result_id,
    action,
    performed_by,
    remarks
)
SELECT
    r.result_id,
    'LEGACY_MIGRATION',
    1,
    'Result migrated from the old Student Result Management System'
FROM result r
WHERE r.workflow_status = 'PUBLISHED';


-- ADD AUDIT LOG FOR MIGRATED RESULTS

INSERT INTO audit_logs
(
    user_id,
    action,
    entity_type,
    entity_id,
    remarks
)
SELECT
    1,
    'LEGACY_MIGRATION',
    'RESULT',
    r.result_id::VARCHAR,
    'Existing result migrated to enterprise result management system'
FROM result r
WHERE r.workflow_status = 'PUBLISHED';