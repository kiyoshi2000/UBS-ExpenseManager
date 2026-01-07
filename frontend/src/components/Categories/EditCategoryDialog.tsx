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

export interface EditCategoryFormData {
  name: string;
  dailyBudget: number;
  dailyBudgetCurrency: string;
  monthlyBudget: number;
  monthlyBudgetCurrency: string;
}

interface EditCategoryDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  category: EditCategoryFormData | null;
  onSubmit: (data: EditCategoryFormData) => void;
  error?: string;
}

const currencyOptions = ["USD", "BRL"];

export const EditCategoryDialog = ({
  open,
  onOpenChange,
  category,
  onSubmit,
  error,
}: EditCategoryDialogProps) => {
  const [formData, setFormData] = useState<EditCategoryFormData>({
    name: "",
    dailyBudget: 0,
    dailyBudgetCurrency: "USD",
    monthlyBudget: 0,
    monthlyBudgetCurrency: "USD",
  });

  const [errors, setErrors] =
    useState<Partial<Record<keyof EditCategoryFormData, string>>>({});

  /** Sync when category changes */
  useEffect(() => {
    if (category && open) {
      setFormData(category);
      setErrors({});
    }
  }, [category, open]);

  const handleInputChange = (
    field: keyof EditCategoryFormData,
    value: string | number
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));

    const newErrors = { ...errors };

    if (field === "name") {
      if (!String(value).trim()) newErrors.name = "Name is required";
      else delete newErrors.name;
    }

    if (field === "dailyBudget") {
      if (Number(value) < 0) newErrors.dailyBudget = "Daily budget must be ≥ 0";
      else delete newErrors.dailyBudget;
    }

    if (field === "dailyBudgetCurrency") {
      if (!value) newErrors.dailyBudgetCurrency = "Currency is required";
      else delete newErrors.dailyBudgetCurrency;
    }

    if (field === "monthlyBudget") {
      if (Number(value) < 0) newErrors.monthlyBudget = "Monthly budget must be ≥ 0";
      else delete newErrors.monthlyBudget;
    }

    if (field === "monthlyBudgetCurrency") {
      if (!value) newErrors.monthlyBudgetCurrency = "Currency is required";
      else delete newErrors.monthlyBudgetCurrency;
    }

    setErrors(newErrors);
  };

  const isFormValid =
    Object.keys(errors).length === 0 &&
    formData.name.trim() !== "" &&
    formData.dailyBudget >= 0 &&
    formData.dailyBudgetCurrency &&
    formData.monthlyBudget >= 0 &&
    formData.monthlyBudgetCurrency;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (isFormValid) onSubmit(formData);
  };

  const handleOpenChange = (newOpen: boolean) => {
    if (!newOpen) setErrors({});
    onOpenChange(newOpen);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Edit Category</DialogTitle>
          <DialogDescription>
            Update the category information below
          </DialogDescription>
        </DialogHeader>

        {error && (
          <div className="flex items-start gap-3 rounded-lg bg-red-50 p-4 dark:bg-red-950/50">
            <AlertCircle className="h-5 w-5 text-red-600 dark:text-red-400 mt-0.5" />
            <div>
              <h3 className="text-sm font-medium text-red-800 dark:text-red-300">
                Error updating category
              </h3>
              <p className="mt-1 text-sm text-red-700 dark:text-red-400">
                {error}
              </p>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Name */}
          <div className="space-y-2">
            <Label htmlFor="name">
              Name <span className="text-red-600">*</span>
            </Label>
            <Input
              id="name"
              value={formData.name}
              onChange={(e) => handleInputChange("name", e.target.value)}
              className={errors.name ? "border-red-500" : ""}
            />
            {errors.name && (
              <p className="text-sm text-red-600">{errors.name}</p>
            )}
          </div>

          {/* Daily Budget */}
          <div className="space-y-2">
            <div className="flex gap-2 items-end">
              <div className="flex flex-col">
                <Label>
                  Currency <span className="text-red-600">*</span>
                </Label>
                <select
                  value={formData.dailyBudgetCurrency}
                  onChange={(e) =>
                    handleInputChange("dailyBudgetCurrency", e.target.value)
                  }
                  className={`h-9 w-24 rounded-md border px-2 text-sm ${
                    errors.dailyBudgetCurrency
                      ? "border-red-500"
                      : "border-input"
                  }`}
                >
                  <option value="">Select</option>
                  {currencyOptions.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex-1 flex flex-col">
                <Label>
                  Daily Budget <span className="text-red-600">*</span>
                </Label>
                <Input
                  type="number"
                  min={0}
                  value={formData.dailyBudget}
                  onChange={(e) =>
                    handleInputChange(
                      "dailyBudget",
                      Number(e.target.value)
                    )
                  }
                  className={errors.dailyBudget ? "border-red-500" : ""}
                />
              </div>
            </div>
          </div>

          {/* Monthly Budget */}
          <div className="space-y-2">
            <div className="flex gap-2 items-end">
              <div className="flex flex-col">
                <Label>
                  Currency <span className="text-red-600">*</span>
                </Label>
                <select
                  value={formData.monthlyBudgetCurrency}
                  onChange={(e) =>
                    handleInputChange("monthlyBudgetCurrency", e.target.value)
                  }
                  className={`h-9 w-24 rounded-md border px-2 text-sm ${
                    errors.monthlyBudgetCurrency
                      ? "border-red-500"
                      : "border-input"
                  }`}
                >
                  <option value="">Select</option>
                  {currencyOptions.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex-1 flex flex-col">
                <Label>
                  Monthly Budget <span className="text-red-600">*</span>
                </Label>
                <Input
                  type="number"
                  min={0}
                  value={formData.monthlyBudget}
                  onChange={(e) =>
                    handleInputChange(
                      "monthlyBudget",
                      Number(e.target.value)
                    )
                  }
                  className={errors.monthlyBudget ? "border-red-500" : ""}
                />
              </div>
            </div>
          </div>

          <DialogFooter className="gap-2 pt-4">
            <Button
              type="button"
              variant="outline"
              size="lg"
              onClick={() => handleOpenChange(false)}
            >
              Cancel
            </Button>
            <Button type="submit" size="lg" disabled={!isFormValid}>
              Save Changes
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
