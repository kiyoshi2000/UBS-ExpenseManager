/**
 * Valida se o email está preenchido e tem formato válido
 */
export const validateEmail = (email: string): string | null => {
  const trimmed = email.trim();

  // Campo vazio
  if (!trimmed) {
    return 'Email é obrigatório';
  }

  // Formato inválido
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(trimmed)) {
    return 'Formato de email inválido';
  }

  return null; // Sem erro
};

/**
 * Valida se a senha está preenchida
 */
export const validatePassword = (password: string): string | null => {
  if (!password) {
    return 'Senha é obrigatória';
  }

  return null; // Sem erro
};

import { z } from "zod";
/* =======================
   DEPARTMENT
======================= */

export const departmentSchema = z.object({
  name: z.string().min(1, "Name is required"),
  monthlyBudget: z
    .number()
    .min(0, "Monthly budget must be positive"),
  currency: z.string().min(1, "Currency is required"),
});

export type DepartmentFormData = z.infer<typeof departmentSchema>;