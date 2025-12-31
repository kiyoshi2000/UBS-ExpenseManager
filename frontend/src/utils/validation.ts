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
}

/**
 * User validation errors interface
 */
export interface UserValidationErrors {
  [key: string]: string;
}

/**
 * Checks if a string is a valid email format
 */
const isValidEmailFormat = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

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
  } else if (!isValidEmailFormat(formData.email)) {
    errors.email = "Invalid email";
  }

  // Role validation
  if (!formData.role) {
    errors.role = "Role is required";
  }

  // Manager email validation
  if (formData.role === "employee" && !formData.managerEmail.trim()) {
    errors.managerEmail = "Manager email is required for employees";
  } else if (formData.managerEmail && !isValidEmailFormat(formData.managerEmail)) {
    errors.managerEmail = "Invalid manager email";
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
