UPDATE contracts
SET status = 'DRAFT'
WHERE status IS NULL OR TRIM(status) = '';

ALTER TABLE contracts
    MODIFY COLUMN status VARCHAR(50) NOT NULL DEFAULT 'DRAFT';
