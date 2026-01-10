CREATE TABLE expense_categories_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    name VARCHAR(255),
    daily_budget DECIMAL(15,2),
    monthly_budget DECIMAL(15,2),
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_expense_categories_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
);
