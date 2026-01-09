import { z } from "zod";

export const departmentSchema = z.object({
  name: z.string().min(1, "Name is required"),
  monthlyBudget: z
    .number()
    .min(0, "Monthly budget must be >= 0"),
  dailyBudget: z
    .number()
    .min(0, "Daily budget must be >= 0")
    .optional(),
  currency: z.enum(["USD", "BRL"], ),
});

export type DepartmentFormData = z.infer<typeof departmentSchema>;
