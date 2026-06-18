UPDATE contracts
SET status = CASE
    WHEN status IN ('TERMINATED', 'EXPIRED', 'CLOSED') THEN 'CLOSED'
    ELSE 'ACTIVE'
END;

ALTER TABLE contracts
    ADD COLUMN contract_type VARCHAR(30) NULL AFTER end_date,
    ADD COLUMN gross_salary DECIMAL(10,2) NULL AFTER contract_type;

UPDATE contracts
SET contract_type = CASE WHEN end_date IS NULL THEN 'PERMANENT' ELSE 'FIXED_TERM' END,
    gross_salary = 0.01
WHERE contract_type IS NULL OR gross_salary IS NULL;

ALTER TABLE contracts
    MODIFY COLUMN contract_type VARCHAR(30) NOT NULL,
    MODIFY COLUMN gross_salary DECIMAL(10,2) NOT NULL,
    MODIFY COLUMN status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN active_employee_id INT
        GENERATED ALWAYS AS (
            CASE WHEN status = 'ACTIVE' AND is_active = 1 THEN employee_id ELSE NULL END
        ) STORED,
    ADD UNIQUE KEY uq_contract_active_employee (active_employee_id);