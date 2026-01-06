import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  departmentSchema,
  DepartmentFormData,
} from "@/utils/validation";
import { Department } from "../types/department";

interface Props {
  initialData?: Department | null;
  onSubmit: (data: DepartmentFormData) => Promise<void>;
}

export const DepartmentForm = ({ initialData, onSubmit }: Props) => {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting, isValid },
  } = useForm<DepartmentFormData>({
    resolver: zodResolver(departmentSchema),
    mode: "onTouched",
    defaultValues: {
      name: initialData?.name ?? "",
      monthlyBudget: initialData?.monthlyBudget ?? 0,
      dailyBudget: initialData?.dailyBudget,
      currency: initialData?.currency ?? "USD",
    },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      {/* Name */}
      <div className="space-y-2">
        <Label>Name *</Label>
        <Input {...register("name")} />
        {errors.name && <p className="text-sm text-red-600">{errors.name.message}</p>}
      </div>

      {/* Monthly Budget */}
      <div className="space-y-2">
        <Label>Monthly Budget *</Label>
        <Input
          type="number"
          {...register("monthlyBudget", { valueAsNumber: true })}
        />
        {errors.monthlyBudget && (
          <p className="text-sm text-red-600">{errors.monthlyBudget.message}</p>
        )}
      </div>

      {/* Daily Budget */}
      <div className="space-y-2">
        <Label>Daily Budget (optional)</Label>
        <Input
          type="number"
          {...register("dailyBudget", { valueAsNumber: true })}
        />
        {errors.dailyBudget && (
          <p className="text-sm text-red-600">{errors.dailyBudget.message}</p>
        )}
      </div>

      {/* Currency */}
      <div className="space-y-2">
        <Label>Currency *</Label>
        <select
          {...register("currency")}
          className="flex h-9 w-full rounded-md border px-3 text-sm"
        >
          <option value="USD">USD</option>
          <option value="BRL">BRL</option>
        </select>
        {errors.currency && (
          <p className="text-sm text-red-600">{errors.currency.message}</p>
        )}
      </div>

      <div className="flex justify-end gap-2 pt-4">
        <button
          type="submit"
          disabled={!isValid || isSubmitting}
          className={`h-10 rounded px-4 text-white ${
            isValid
              ? "bg-blue-600 hover:bg-blue-700"
              : "bg-gray-400 cursor-not-allowed"
          }`}
        >
          {isSubmitting ? "Saving..." : "Save"}
        </button>
      </div>
    </form>
  );
};
