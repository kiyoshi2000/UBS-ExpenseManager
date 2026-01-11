import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { MoneyInput } from "@/components/ui/money-input";
import {
  departmentSchema,
  DepartmentFormData,
} from "@/utils/validation";
import type { Department, Currency } from "@/types/department";
import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { getCurrencies } from "@/api/currency.api";

interface Props {
  initialData?: Department | null;
  onSubmit: (data: DepartmentFormData) => Promise<void>;
}

export const DepartmentForm = ({ initialData, onSubmit }: Props) => {
  const [currencies, setCurrencies] = useState<Currency[]>([]);
  const [loadingCurrencies, setLoadingCurrencies] = useState(true);

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
      currencyId: initialData?.currencyId ?? 0,
    },
  });

  useEffect(() => {
    const fetchCurrencies = async () => {
      try {
        setLoadingCurrencies(true);
        const currenciesList = await getCurrencies();
        setCurrencies(currenciesList);
      } catch (error) {
        console.error("Failed to load currencies:", error);
      } finally {
        setLoadingCurrencies(false);
      }
    };

    fetchCurrencies();
  }, []);

  const selectedCurrencyId = watch("currencyId");
  const selectedCurrency = currencies.find(c => c.id === selectedCurrencyId);

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
        <Label htmlFor="currencyId">
          Currency <span className="text-red-600">*</span>
        </Label>
        <select
          id="currencyId"
          className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
          {...register("currencyId", { valueAsNumber: true })}
          disabled={loadingCurrencies}
        >
          <option value={0}>
            {loadingCurrencies ? "Loading currencies..." : "Select currency"}
          </option>
          {currencies.map((currency) => (
            <option key={currency.id} value={currency.id}>
              {currency.name}
            </option>
          ))}
        </select>
        {errors.currencyId && (
          <p className="text-sm text-red-600">{errors.currencyId.message}</p>
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
              currency={selectedCurrency?.name || "USD"}
              onChange={field.onChange}
              disabled={!selectedCurrencyId}
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
              currency={selectedCurrency?.name || "USD"}
              onChange={field.onChange}
              disabled={!selectedCurrencyId}
            />
          )}
        />
        {errors.dailyBudget && (
          <p className="text-sm text-red-600">{errors.dailyBudget.message}</p>
        )}
      </div>

      <Button type="submit" disabled={!isValid || isSubmitting || loadingCurrencies}>
        {isSubmitting ? "Saving..." : initialData ? "Update" : "Create"}
      </Button>
    </form>
  );
};