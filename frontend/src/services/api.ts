import axios from "axios";

/**
 * Centralized Axios instance for the application.
 *
 * This instance is responsible for:
 * - Defining the backend base URL
 * - Automatically attaching the JWT token to every request
 *
 * All API calls (auth, departments, etc.) must use this instance
 * to ensure consistency and security.
 */
export const api = axios.create({
  /**
   * Base URL of the backend API.
   *
   * - Uses VITE_API_BASE_URL if defined in environment variables
   * - Falls back to localhost for local development
   */
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
});

/**
 * Axios request interceptor.
 *
 * This interceptor runs BEFORE every HTTP request
 * and injects the JWT token (if present) into the Authorization header.
 *
 * This allows the backend (Spring Security) to:
 * - Authenticate the user
 * - Authorize requests based on roles (FINANCE, MANAGER, EMPLOYEE)
 */
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("jwt_token");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

export default api;
