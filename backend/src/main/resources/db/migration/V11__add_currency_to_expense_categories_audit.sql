-- Add currency_id column to expense_categories_AUD (audit table)
ALTER TABLE expense_categories_AUD
ADD COLUMN currency_id BIGINT;

-- Update existing audit records to use USD as default currency
UPDATE expense_categories_AUD
SET currency_id = (SELECT id FROM currencies WHERE name = 'USD')
WHERE currency_id IS NULL;
