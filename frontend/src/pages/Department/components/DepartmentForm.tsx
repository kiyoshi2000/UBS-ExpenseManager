import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { MoneyInput } from "@/components/ui/money-input";
import {
  departmentSchema,
  DepartmentFormData,
} from "@/utils/validation";
import type { Department } from "@/types/department";
import { Button } from "@/components/ui/button";

interface Props {
  initialData?: Department | null;
  onSubmit: (data: DepartmentFormData) => Promise<void>;
}

export const DepartmentForm = ({ initialData, onSubmit }: Props) => {
  const {
    register,
    handleSubmit,
    control,
    watch,
    formState: { errors, isSubmitting, isValid },
  } = useForm<DepartmentFormData>({
    resolver: zodResolver(departmentSchema),
    mode: "onTouched",
    defaultValues: {
      name: initialData?.name ?? "",
      monthlyBudget: initialData?.monthlyBudget ?? 0,
      dailyBudget: initialData?.dailyBudget ?? null,
      currency: initialData?.currency ?? "USD",
    },
  });

  const selectedCurrency = watch("currency");

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      {/* Name */}
      <div className="space-y-2">
        <Label htmlFor="name">
          Name <span className="text-red-600">*</span>
        </Label>
        <Input
          id="name"
          placeholder="Engineering"
          {...register("name")}
        />
        {errors.name && (
          <p className="text-sm text-red-600">{errors.name.message}</p>
        )}
      </div>

      {/* Currency */}
      <div className="space-y-2">
        <Label htmlFor="currency">
          Currency <span className="text-red-600">*</span>
        </Label>
        <select
          id="currency"
          className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
          {...register("currency")}
        >
          <option value="">Select currency</option>
          <option value="USD">USD</option>
          <option value="BRL">BRL</option>
        </select>
        {errors.currency && (
          <p className="text-sm text-red-600">{errors.currency.message}</p>
        )}
      </div>

      {/* Monthly Budget */}
      <div className="space-y-2">
        <Label htmlFor="monthlyBudget">
          Monthly Budget <span className="text-red-600">*</span>
        </Label>
        <Controller
          name="monthlyBudget"
          control={control}
          render={({ field }) => (
            <MoneyInput
              id="monthlyBudget"
              placeholder="0.00"
              value={field.value}
              currency={selectedCurrency}
              onChange={field.onChange}
              disabled={!selectedCurrency}
            />
          )}
        />
        {errors.monthlyBudget && (
          <p className="text-sm text-red-600">{errors.monthlyBudget.message}</p>
        )}
      </div>

      {/* Daily Budget (Optional) */}
      <div className="space-y-2">
        <Label htmlFor="dailyBudget">Daily Budget (Optional)</Label>
        <Controller
          name="dailyBudget"
          control={control}
          render={({ field }) => (
            <MoneyInput
              id="dailyBudget"
              placeholder="0.00"
              value={field.value}
              currency={selectedCurrency}
              onChange={field.onChange}
              disabled={!selectedCurrency}
            />
          )}
        />
        {errors.dailyBudget && (
          <p className="text-sm text-red-600">{errors.dailyBudget.message}</p>
        )}
      </div>

      <Button type="submit" disabled={!isValid || isSubmitting}>
        {isSubmitting ? "Saving..." : initialData ? "Update" : "Create"}
      </Button>
    </form>
  );
};