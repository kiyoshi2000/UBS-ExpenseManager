import * as React from 'react';
import { Input } from './input';
import { cn } from '@/lib/utils';

// Utility functions for money handling
const parseMoneyValue = (value: string): number | null => {
  if (!value || value === '') return null;
  const numberValue = parseFloat(value);
  if (isNaN(numberValue)) return null;
  return numberValue;
};

const formatMoneyValue = (amount: number | null): string => {
  if (amount === null) return '';
  
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
};

export interface MoneyInputProps extends Omit<React.ComponentProps<'input'>, 'value' | 'onChange'> {
  value: number | null;
  currency: string;
  onChange: (value: number | null) => void;
}

const MoneyInput = React.forwardRef<HTMLInputElement, MoneyInputProps>(
  ({ className, value, currency, onChange, disabled, ...props }, ref) => {
    const [displayValue, setDisplayValue] = React.useState('');
    const [isFocused, setIsFocused] = React.useState(false);

    // Update display value when value changes from outside (not during typing)
    React.useEffect(() => {
      if (!isFocused) {
        setDisplayValue(value !== null ? value.toString() : '');
      }
    }, [value, isFocused]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const inputValue = e.target.value;
      
      // Allow empty input
      if (inputValue === '') {
        setDisplayValue('');
        onChange(null);
        return;
      }

      // Only allow numbers and a single decimal point
      // Allow partial inputs like "5." or ".5"
      const regex = /^\d*\.?\d{0,2}$/;
      
      if (regex.test(inputValue)) {
        setDisplayValue(inputValue);
        
        // Parse and send the value
        const parsedValue = parseMoneyValue(inputValue);
        onChange(parsedValue);
      }
    };

    const handleFocus = () => {
      setIsFocused(true);
      // Show raw number when focused
      if (value !== null) {
        setDisplayValue(value.toString());
      }
    };

    const handleBlur = () => {
      setIsFocused(false);
      // Format the number when blurred
      if (value !== null) {
        // Ensure we have a valid number and format it properly
        const formatted = value.toFixed(2);
        setDisplayValue(formatted);
      }
    };

    return (
      <div className="relative flex w-full">
        <div className={cn(
          "flex h-9 items-center rounded-l-md border border-r-0 border-input bg-transparent px-3 text-sm shadow-xs",
          disabled && "opacity-50 cursor-not-allowed bg-muted"
        )}>
          <span className="font-medium text-muted-foreground whitespace-nowrap">
            {currency || '---'}
          </span>
        </div>
        <Input
          ref={ref}
          type="text"
          inputMode="decimal"
          placeholder="0.00"
          value={displayValue}
          onChange={handleChange}
          onFocus={handleFocus}
          onBlur={handleBlur}
          disabled={disabled}
          className={cn("rounded-l-none focus-visible:ring-offset-0", className)}
          {...props}
        />
      </div>
    );
  }
);

MoneyInput.displayName = 'MoneyInput';

export { MoneyInput, parseMoneyValue, formatMoneyValue };
