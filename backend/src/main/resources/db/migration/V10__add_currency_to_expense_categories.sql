-- Add currency_id column to expense_categories table
ALTER TABLE expense_categories
ADD COLUMN currency_id BIGINT;

-- Set default currency to USD for existing records (if any)
UPDATE expense_categories
SET currency_id = (SELECT id FROM currencies WHERE name = 'USD')
WHERE currency_id IS NULL;

-- Make currency_id NOT NULL after setting defaults
ALTER TABLE expense_categories
ALTER COLUMN currency_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE expense_categories
ADD CONSTRAINT fk_expense_categories_currency
FOREIGN KEY (currency_id) REFERENCES currencies(id);
