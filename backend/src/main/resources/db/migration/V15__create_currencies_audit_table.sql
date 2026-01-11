CREATE TABLE IF NOT EXISTS currencies_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    name VARCHAR(3),
    exchange_rate DECIMAL(15,6),
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_currencies_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev) DEFERRABLE INITIALLY DEFERRED
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE tablename = 'currencies_aud' AND indexname = 'idx_currencies_aud_rev') THEN
        CREATE INDEX idx_currencies_aud_rev ON currencies_aud(rev);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE tablename = 'currencies_aud' AND indexname = 'idx_currencies_aud_id') THEN
        CREATE INDEX idx_currencies_aud_id ON currencies_aud(id);
    END IF;
END $$;
