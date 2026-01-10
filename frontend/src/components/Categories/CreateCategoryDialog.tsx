import { useState, useEffect } from "react";
import { AlertCircle } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { CreateCategoryFormData } from "@/utils/validation";

interface CreateCategoryDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: CreateCategoryFormData) => void;
  error?: string;
}

const currencyOptions = ["USD", "BRL"];

/* Utils */
const parseMoneyToCents = (value: string): number | null => {
  if (!value) return null;
  const cleaned = value.replace(/[^\d.]/g, "");
  const numberValue = Number(cleaned);
  if (isNaN(numberValue)) return null;
  return Math.round(numberValue * 100);
};

const formatCentsToCurrency = (
  cents: number | null,
  currency: string
): string => {
  if (cents === null || !currency) return "";

  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency,
    minimumFractionDigits: 2,
  }).format(cents / 100);
};

export const CreateCategoryDialog = ({
  open,
  onOpenChange,
  onSubmit,
  error,
}: CreateCategoryDialogProps) => {
  const [formData, setFormData] = useState<CreateCategoryFormData>({
    name: "",
    dailyBudget: 0,
    monthlyBudget: 0,
    currencyName: "",
  });

  const [errors, setErrors] = useState<
    Partial<Record<keyof CreateCategoryFormData, string>>
  >({});

  useEffect(() => {
    if (!open) handleReset();
  }, [open]);

  const handleInputChange = (
    field: keyof CreateCategoryFormData,
    value: string
  ) => {
    let parsedValue: any = value;

    if (field === "dailyBudget" || field === "monthlyBudget") {
      parsedValue = parseMoneyToCents(value);
    }

    setFormData((prev) => ({
      ...prev,
      [field]: parsedValue,
    }));

    const newErrors = { ...errors };

    if (field === "name") {
      if (!value.trim()) newErrors.name = "Name is required";
      else delete newErrors.name;
    }

    if (field === "currencyName") {
      if (!value) newErrors.currencyName = "Currency is required";
      else delete newErrors.currencyName;
    }

    if (field === "dailyBudget") {
      if (parsedValue === null || parsedValue < 0)
        newErrors.dailyBudget = "Daily budget must be ≥ 0";
      else delete newErrors.dailyBudget;
    }

    if (field === "monthlyBudget") {
      if (parsedValue === null || parsedValue < 0)
        newErrors.monthlyBudget = "Monthly budget must be ≥ 0";
      else delete newErrors.monthlyBudget;
    }

    setErrors(newErrors);
  };

  const isFormValid = () =>
    Object.keys(errors).length === 0 &&
    formData.name.trim() !== "" &&
    formData.currencyName !== "" &&
    formData.dailyBudget !== null &&
    formData.monthlyBudget !== null;

  const handleReset = () => {
    setFormData({
      name: "",
      dailyBudget: 0,
      monthlyBudget: 0,
      currencyName: "",
    });
    setErrors({});
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (isFormValid()) onSubmit(formData);
  };

  const handleOpenChange = (newOpen: boolean) => {
    if (!newOpen) handleReset();
    onOpenChange(newOpen);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-[500px]"
        onInteractOutside={(e) => e.preventDefault()}
        onEscapeKeyDown={(e) => e.preventDefault()}
      >
        <DialogHeader>
          <DialogTitle>Create New Category</DialogTitle>
          <DialogDescription>
            Fill in the details of the new category below
          </DialogDescription>
        </DialogHeader>

        {error && (
          <div className="flex items-start gap-3 rounded-lg bg-red-50 p-4 dark:bg-red-950/50">
            <AlertCircle className="h-5 w-5 text-red-600 mt-0.5" />
            <div>
              <h3 className="text-sm font-medium text-red-800">
                Error creating category
              </h3>
              <p className="mt-1 text-sm text-red-700">{error}</p>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Name */}
          <div className="space-y-2">
            <Label>
              Name <span className="text-red-600">*</span>
            </Label>
            <Input
              placeholder="Marketing"
              value={formData.name}
              onChange={(e) =>
                handleInputChange("name", e.target.value)
              }
            />
            {errors.name && (
              <p className="text-sm text-red-600">{errors.name}</p>
            )}
          </div>

          {/* Currency */}
          <div className="space-y-2">
            <Label>
              Currency <span className="text-red-600">*</span>
            </Label>
            <select
              value={formData.currencyName}
              onChange={(e) =>
                handleInputChange("currencyName", e.target.value)
              }
              className="flex h-9 w-full rounded-md border px-3 py-1 text-sm"
            >
              <option value="">Select</option>
              {currencyOptions.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
            {errors.currencyName && (
              <p className="text-sm text-red-600">
                {errors.currencyName}
              </p>
            )}
          </div>

          {/* Daily Budget */}
          <div className="space-y-2">
            <Label>
              Daily Budget <span className="text-red-600">*</span>
            </Label>
            <Input
              placeholder="0.00"
              value={formatCentsToCurrency(
                formData.dailyBudget,
                formData.currencyName
              )}
              onChange={(e) =>
                handleInputChange("dailyBudget", e.target.value)
              }
              disabled={!formData.currencyName}
            />
            {errors.dailyBudget && (
              <p className="text-sm text-red-600">
                {errors.dailyBudget}
              </p>
            )}
          </div>

          {/* Monthly Budget */}
          <div className="space-y-2">
            <Label>
              Monthly Budget <span className="text-red-600">*</span>
            </Label>
            <Input
              placeholder="0.00"
              value={formatCentsToCurrency(
                formData.monthlyBudget,
                formData.currencyName
              )}
              onChange={(e) =>
                handleInputChange("monthlyBudget", e.target.value)
              }
              disabled={!formData.currencyName}
            />
            {errors.monthlyBudget && (
              <p className="text-sm text-red-600">
                {errors.monthlyBudget}
              </p>
            )}
          </div>

          <DialogFooter className="pt-4 gap-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => handleOpenChange(false)}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={!isFormValid()}>
              Create Category
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
