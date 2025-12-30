/**
 * Authentication-related types.
 *
 * These types mirror the backend authentication DTOs
 * and should stay in sync with the API contract.
 */

export type UserRole = "FINANCE" | "MANAGER" | "EMPLOYEE";

export interface AuthUser {
  id: number;
  email: string;
  role: UserRole;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: AuthUser;
}
