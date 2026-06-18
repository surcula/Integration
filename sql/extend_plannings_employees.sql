ALTER TABLE plannings_employees
    ADD COLUMN note        VARCHAR(500)     NULL,
    ADD COLUMN performed   TINYINT(1)  NOT NULL DEFAULT 0;
