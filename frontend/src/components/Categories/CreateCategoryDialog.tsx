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

export const CreateCategoryDialog = ({
  open,
  onOpenChange,
  onSubmit,
  error,
}: CreateCategoryDialogProps) => {
  const [formData, setFormData] = useState<CreateCategoryFormData>({
    name: "",
    dailyBudget: "",
    dailyBudgetCurrency: "",
    monthlyBudget: "",
    monthlyBudgetCurrency: "",
  });

  const [errors, setErrors] = useState<Partial<Record<keyof CreateCategoryFormData, string>>>({});

  useEffect(() => {
    if (!open) handleReset();
  }, [open]);

  const handleInputChange = (field: keyof CreateCategoryFormData, value: string) => {
    setFormData({ ...formData, [field]: value });

    const newErrors = { ...errors };

    if (field === "name") {
      if (!value.trim()) newErrors.name = "Name is required";
      else delete newErrors.name;
    }

    if (field === "dailyBudget") {
      if (!value || Number(value.replace(/[^\d.]/g, "")) < 0)
        newErrors.dailyBudget = "Daily budget must be ≥ 0";
      else delete newErrors.dailyBudget;
    }

    if (field === "dailyBudgetCurrency") {
      if (!value) newErrors.dailyBudgetCurrency = "Currency is required";
      else delete newErrors.dailyBudgetCurrency;
    }

    if (field === "monthlyBudget") {
      if (!value || Number(value.replace(/[^\d.]/g, "")) < 0)
        newErrors.monthlyBudget = "Monthly budget must be ≥ 0";
      else delete newErrors.monthlyBudget;
    }

    if (field === "monthlyBudgetCurrency") {
      if (!value) newErrors.monthlyBudgetCurrency = "Currency is required";
      else delete newErrors.monthlyBudgetCurrency;
    }

    setErrors(newErrors);
  };

  const formatCurrency = (value: string, currency: string) => {
    if (!value) return "";
    const numericValue = Number(value.replace(/[^\d.]/g, ""));
    if (isNaN(numericValue)) return "";
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: currency || "USD",
      minimumFractionDigits: 2,
    }).format(numericValue);
  };

  const isFormValid = () =>
    Object.keys(errors).length === 0 &&
    formData.name.trim() !== "" &&
    formData.dailyBudget &&
    formData.dailyBudgetCurrency &&
    formData.monthlyBudget &&
    formData.monthlyBudgetCurrency;

  const handleReset = () => {
    setFormData({
      name: "",
      dailyBudget: "",
      dailyBudgetCurrency: "",
      monthlyBudget: "",
      monthlyBudgetCurrency: "",
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
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Create New Category</DialogTitle>
          <DialogDescription>
            Fill in the details of the new category below
          </DialogDescription>
        </DialogHeader>

        {error && (
          <div className="flex items-start gap-3 rounded-lg bg-red-50 p-4 dark:bg-red-950/50">
            <AlertCircle className="h-5 w-5 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
            <div className="flex-1">
              <h3 className="text-sm font-medium text-red-800 dark:text-red-300">
                Error creating category
              </h3>
              <p className="mt-1 text-sm text-red-700 dark:text-red-400">{error}</p>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Name */}
          <div className="space-y-2">
            <Label htmlFor="name" className="text-sm font-medium">
              Name <span className="text-red-600">*</span>
            </Label>
            <Input
              id="name"
              placeholder="Marketing"
              value={formData.name}
              onChange={(e) => handleInputChange("name", e.target.value)}
              className={errors.name ? "border-red-500" : ""}
            />
            {errors.name && <p className="text-sm text-red-600">{errors.name}</p>}
          </div>

          {/* Daily Budget */}
          <div className="space-y-2">
            <div className="flex gap-2 items-end">
              {/* Currency Select */}
              <div className="flex flex-col">
                <Label htmlFor="dailyBudgetCurrency" className="text-sm font-medium">
                  Currency <span className="text-red-600">*</span>
                </Label>
                <select
                  id="dailyBudgetCurrency"
                  value={formData.dailyBudgetCurrency}
                  onChange={(e) => handleInputChange("dailyBudgetCurrency", e.target.value)}
                  className={`flex h-9 w-24 rounded-md border px-2 py-1 text-sm ${
                    errors.dailyBudgetCurrency ? "border-red-500" : "border-input"
                  }`}
                >
                  <option value="">Select</option>
                  {currencyOptions.map((c) => (
                    <option key={c} value={c}>{c}</option>
                  ))}
                </select>
                {errors.dailyBudgetCurrency && <p className="text-xs text-red-600">{errors.dailyBudgetCurrency}</p>}
              </div>

              {/* Budget Input */}
              <div className="flex-1 flex flex-col">
                <Label htmlFor="dailyBudget" className="text-sm font-medium">Daily Budget <span className="text-red-600">*</span></Label>
                <Input
                  id="dailyBudget"
                  placeholder="0.00"
                  value={formatCurrency(formData.dailyBudget, formData.dailyBudgetCurrency)}
                  onChange={(e) => handleInputChange("dailyBudget", e.target.value)}
                  className={errors.dailyBudget ? "border-red-500" : ""}
                  disabled={!formData.dailyBudgetCurrency}
                />
                {errors.dailyBudget && <p className="text-xs text-red-600">{errors.dailyBudget}</p>}
              </div>
            </div>
          </div>

          {/* Monthly Budget */}
          <div className="space-y-2">
            <div className="flex gap-2 items-end">
              {/* Currency Select */}
              <div className="flex flex-col">
                <Label htmlFor="monthlyBudgetCurrency" className="text-sm font-medium">
                  Currency <span className="text-red-600">*</span>
                </Label>
                <select
                  id="monthlyBudgetCurrency"
                  value={formData.monthlyBudgetCurrency}
                  onChange={(e) => handleInputChange("monthlyBudgetCurrency", e.target.value)}
                  className={`flex h-9 w-24 rounded-md border px-2 py-1 text-sm ${
                    errors.monthlyBudgetCurrency ? "border-red-500" : "border-input"
                  }`}
                >
                  <option value="">Select</option>
                  {currencyOptions.map((c) => (
                    <option key={c} value={c}>{c}</option>
                  ))}
                </select>
                {errors.monthlyBudgetCurrency && <p className="text-xs text-red-600">{errors.monthlyBudgetCurrency}</p>}
              </div>

              {/* Budget Input */}
              <div className="flex-1 flex flex-col">
                <Label htmlFor="monthlyBudget" className="text-sm font-medium">Monthly Budget <span className="text-red-600">*</span></Label>
                <Input
                  id="monthlyBudget"
                  placeholder="0.00"
                  value={formatCurrency(formData.monthlyBudget, formData.monthlyBudgetCurrency)}
                  onChange={(e) => handleInputChange("monthlyBudget", e.target.value)}
                  className={errors.monthlyBudget ? "border-red-500" : ""}
                  disabled={!formData.monthlyBudgetCurrency}
                />
                {errors.monthlyBudget && <p className="text-xs text-red-600">{errors.monthlyBudget}</p>}
              </div>
            </div>
          </div>

          <DialogFooter className="gap-2 pt-4">
            <Button type="button" variant="outline" size="lg" onClick={() => handleOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="default" size="lg" disabled={!isFormValid()}>
              Create Category
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
