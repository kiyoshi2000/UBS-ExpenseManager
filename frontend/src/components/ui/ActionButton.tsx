import { ReactNode } from "react";
import { Button } from "@/components/ui/button";

interface ActionButtonProps {
  label: string;
  icon?: ReactNode;
  onClick?: () => void;
  variant?: "default" | "secondary" | "destructive" | "outline" | "ghost";
  className?: string;
  disabled?: boolean;
  type?: "button" | "submit" | "reset";
}

export const ActionButton = ({
  label,
  icon,
  onClick,
  variant = "default",
  className = "",
  disabled = false,
  type = "button",
}: ActionButtonProps) => {
  return (
    <Button
      type={type}
      variant={variant}
      onClick={onClick}
      disabled={disabled}
      className={`h-9 flex items-center gap-2 ${className}`}
    >
      {icon}
      {label}
    </Button>
  );
};
