import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";

interface SearchInputProps {
  placeholder: string;
  value?: string;
  onChange?: (value: string) => void;
  className?: string;
}

export const SearchInput = ({
  placeholder,
  value,
  onChange,
  className = "",
}: SearchInputProps) => {
  return (
    <div className={`relative w-full max-w-xs ${className}`}>
      <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
      <Input
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange?.(e.target.value)}
        className="h-9 w-full rounded-lg border border-slate-200 pl-9 shadow-sm placeholder:text-slate-500 focus-visible:ring-0 focus-visible:ring-offset-0"
      />
    </div>
  );
};
