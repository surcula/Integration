ALTER TABLE absences
    ADD COLUMN certificate_validated TINYINT(1) NOT NULL DEFAULT 0 AFTER reviewed_at,
    ADD COLUMN certificate_validated_by INT NULL AFTER certificate_validated,
    ADD COLUMN certificate_validated_at DATETIME NULL AFTER certificate_validated_by,
    ADD CONSTRAINT fk_absences_certificate_validated_by
        FOREIGN KEY (certificate_validated_by) REFERENCES employees(id);
