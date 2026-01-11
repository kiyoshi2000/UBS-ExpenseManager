ALTER TABLE departments 
ADD COLUMN currency_id BIGINT;

UPDATE departments d
SET currency_id = (
    SELECT c.id 
    FROM currencies c 
    WHERE c.name = d.currency
)
WHERE d.currency IS NOT NULL;

ALTER TABLE departments 
ALTER COLUMN currency_id SET NOT NULL;


ALTER TABLE departments
ADD CONSTRAINT fk_departments_currency_id
FOREIGN KEY (currency_id) REFERENCES currencies(id);


ALTER TABLE departments 
DROP COLUMN currency;

