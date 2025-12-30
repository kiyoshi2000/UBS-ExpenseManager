import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { departmentSchema, DepartmentFormData } from "@/utils/validation";
import { Department } from "@/types/department";

/**
 * DepartmentForm
 *
 * Used for both creating and editing departments.
 * Controlled via props to remain reusable.
 */

type Props = {
  initialData?: Department | null;
  onSubmit: (data: DepartmentFormData) => void;
  onCancel: () => void;
};

export const DepartmentForm = ({
  initialData,
  onSubmit,
  onCancel,
}: Props) => {
  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm<DepartmentFormData>({
    resolver: zodResolver(departmentSchema),
    mode: "onTouched",
    defaultValues: {
      name: initialData?.name ?? "",
      monthlyBudget: initialData?.monthlyBudget ?? 0,
      currency: initialData?.currency ?? "USD",
    },
  });

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      className="space-y-4 border p-4 rounded"
    >
      <div>
        <label>Name</label>
        <input {...register("name")} className="input" />
        {errors.name && <p className="text-destructive">{errors.name.message}</p>}
      </div>

      <div>
        <label>Monthly Budget</label>
        <input
          type="number"
          {...register("monthlyBudget", { valueAsNumber: true })}
          className="input"
        />
        {errors.monthlyBudget && (
          <p className="text-destructive">{errors.monthlyBudget.message}</p>
        )}
      </div>

      <div>
        <label>Currency</label>
        <input {...register("currency")} className="input" />
        {errors.currency && (
          <p className="text-destructive">{errors.currency.message}</p>
        )}
      </div>

      <div className="flex gap-2">
        <button type="submit" disabled={!isValid}>
          Save
        </button>
        <button type="button" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  );
};
