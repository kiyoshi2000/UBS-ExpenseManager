/**
 * Validates if email is filled and has valid format
 */
export const validateEmail = (email: string): string | null => {
  const trimmed = email.trim();

  // Empty field
  if (!trimmed) {
    return 'Email is required';
  }

  // Valid format
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(trimmed)) {
    return 'Invalid email format';
  }

  return null; // No error
};

/**
 * Valida se a senha está preenchida
 */
export const validatePassword = (password: string): string | null => {
  if (!password) {
    return 'Password is required';
  }

  return null; // Sem erro
};

/**
 * User form data interface
 */
export interface CreateUserFormData {
  name: string;
  email: string;
  role: "employee" | "manager" | "finance" | "";
  managerEmail: string;
  departmentId: string;
}

/**
 * User validation errors interface
 */
export interface UserValidationErrors {
  [key: string]: string;
}

/**
 * Validates the entire user form
 */
export const validateUserForm = (
  formData: CreateUserFormData
): UserValidationErrors => {
  const errors: UserValidationErrors = {};

  // Name validation
  if (!formData.name.trim()) {
    errors.name = "Name is required";
  }

  // Email validation
  if (!formData.email.trim()) {
    errors.email = "Email is required";
  } else {
    const emailError = validateEmail(formData.email);
    if (emailError) {
      errors.email = emailError;
    }
  }

  // Role validation
  if (!formData.role) {
    errors.role = "Role is required";
  }

  // Department validation
  if (!formData.departmentId) {
    errors.departmentId = "Department is required";
  }

  // Manager email validation
  if (formData.role === "employee" && !formData.managerEmail.trim()) {
    errors.managerEmail = "Manager is required for employees";
  } else if (formData.managerEmail.trim()) {
    const managerEmailError = validateEmail(formData.managerEmail);
    if (managerEmailError) {
      errors.managerEmail = "Invalid manager email";
    }
  }

  return errors;
};

/**
 * Checks if there are any validation errors
 */
export const hasValidationErrors = (
  errors: UserValidationErrors
): boolean => {
  return Object.keys(errors).length > 0;
};

import { z } from "zod";

/* =======================
   DEPARTMENT
======================= */

export const departmentSchema = z.object({
  name: z.string().min(1, "Name is required"),

  monthlyBudget: z
    .number({ invalid_type_error: "Monthly budget is required" })
    .min(0, "Monthly budget must be >= 0"),

  dailyBudget: z
    .number()
    .min(0, "Daily budget must be >= 0")
    .nullable()
    .optional(),

  currency: z.enum(["USD", "BRL"]),
});

export type DepartmentFormData = z.infer<typeof departmentSchema>;
