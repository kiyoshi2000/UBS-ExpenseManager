CREATE TABLE IF NOT EXISTS departments_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    name VARCHAR(255),
    monthly_budget DECIMAL(15,2),
    daily_budget DECIMAL(15,2),
    manager_id BIGINT,
    currency_id BIGINT,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_departments_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev) DEFERRABLE INITIALLY DEFERRED
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE tablename = 'departments_aud' AND indexname = 'idx_departments_aud_rev') THEN
        CREATE INDEX idx_departments_aud_rev ON departments_aud(rev);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE tablename = 'departments_aud' AND indexname = 'idx_departments_aud_id') THEN
        CREATE INDEX idx_departments_aud_id ON departments_aud(id);
    END IF;
END $$;
