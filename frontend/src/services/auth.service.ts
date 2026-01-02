import api from "./api";

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    id: number;
    email: string;
    role: "FINANCE" | "MANAGER" | "EMPLOYEE";
  };
}

export function login(data: LoginRequest) {
  return api.post<LoginResponse>("/api/auth/login", data);
}

export function register(data: {
  email: string;
  password: string;
  role: string;
}) {
  return api.post<LoginResponse>("/api/auth/register", data);
}
