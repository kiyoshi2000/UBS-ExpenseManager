-- Envers revision info table (shared by all audited entities)
CREATE TABLE revinfo (
    rev INTEGER NOT NULL,
    revtstmp BIGINT,
    PRIMARY KEY (rev)
);

CREATE SEQUENCE revinfo_seq START WITH 1 INCREMENT BY 50;
