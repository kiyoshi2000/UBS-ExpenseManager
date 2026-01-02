import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { departmentSchema, DepartmentFormData } from "@/utils/validation";
import { Department } from "@/types/department";

/**
 * DepartmentForm
 *
 * Reusable form for creating and editing departments.
 *
 * Improvements:
 * - Clear context (Create vs Edit)
 * - Strong visual feedback for buttons (enabled/disabled)
 * - Better spacing and readability
 */

type Props = {
  initialData?: Department | null;
  onSubmit: (data: DepartmentFormData) => void;
  onCancel: () => void;
};

export const DepartmentForm = ({ initialData, onSubmit, onCancel }: Props) => {
  const {
    register,
    handleSubmit,
    formState: { errors, isValid, isSubmitting },
  } = useForm<DepartmentFormData>({
    resolver: zodResolver(departmentSchema),
    mode: "onTouched",
    defaultValues: {
      name: initialData?.name ?? "",
      monthlyBudget: initialData?.monthlyBudget ?? 0,
      currency: initialData?.currency ?? "USD",
    },
  });

  const formTitle = initialData ? "Edit Department" : "Create Department";
  const submitLabel = initialData ? "Update" : "Save";

  return (
    <div className="border rounded p-4 bg-muted/30">
      <h2 className="text-xl font-semibold mb-4">{formTitle}</h2>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="space-y-1">
          <label className="text-sm font-medium">Name</label>
          <input
            {...register("name")}
            className="w-full border rounded px-3 py-2"
            placeholder="e.g. HR"
          />
          {errors.name && (
            <p className="text-sm text-destructive">{errors.name.message}</p>
          )}
        </div>

        <div className="space-y-1">
          <label className="text-sm font-medium">Monthly Budget</label>
          <input
            type="number"
            {...register("monthlyBudget", { valueAsNumber: true })}
            className="w-full border rounded px-3 py-2"
            placeholder="e.g. 1000"
          />
          {errors.monthlyBudget && (
            <p className="text-sm text-destructive">
              {errors.monthlyBudget.message}
            </p>
          )}
        </div>

        <div className="space-y-1">
          <label className="text-sm font-medium">Currency</label>
          <input
            {...register("currency")}
            className="w-full border rounded px-3 py-2"
            placeholder="e.g. USD"
          />
          {errors.currency && (
            <p className="text-sm text-destructive">
              {errors.currency.message}
            </p>
          )}
        </div>

        <div className="flex gap-2 pt-2">
          <button
            type="submit"
            disabled={!isValid || isSubmitting}
            className={`px-4 py-2 rounded text-white ${
              !isValid || isSubmitting
                ? "bg-gray-400 cursor-not-allowed"
                : "bg-green-600 hover:bg-green-700"
            }`}
          >
            {isSubmitting ? "Saving..." : submitLabel}
          </button>

          <button
            type="button"
            onClick={onCancel}
            className="px-4 py-2 rounded bg-gray-200 hover:bg-gray-300"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
};
