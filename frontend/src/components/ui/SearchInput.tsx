import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

interface SearchInputProps {
  placeholder: string;
  value?: string;
  onChange?: (value: string) => void;
  onSearch?: () => void;
  className?: string;
}

export const SearchInput = ({
  placeholder,
  value,
  onChange,
  onSearch,
  className = "",
}: SearchInputProps) => {
  return (
    <div className={`flex w-full max-w-md ${className}`}>
      <div className="relative flex-1">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-500" />
        <Input
          placeholder={placeholder}
          value={value}
          onChange={(e) => onChange?.(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter" && onSearch) {
              onSearch();
            }
          }}
          className="h-9 border-r-0 rounded-r-none pl-9 shadow-sm focus-visible:ring-0 focus-visible:ring-offset-0"
        />
      </div>
      <Button
        type="button"
        onClick={onSearch}
        className="h-9 rounded-l-none"
      >
        Search
      </Button>
    </div>
  );
};
