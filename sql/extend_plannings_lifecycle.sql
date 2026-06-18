ALTER TABLE plannings
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' AFTER description,
    ADD COLUMN published_at DATETIME NULL AFTER status,
    ADD COLUMN cancelled_at DATETIME NULL AFTER published_at,
    ADD INDEX idx_plannings_status (status, is_active);

UPDATE plannings
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE is_active = 1;
