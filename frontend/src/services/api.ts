import axios from "axios";

/**
 * Centralized Axios instance for the application.
 *
 * This instance is responsible for:
 * - Defining the backend base URL
 * - Sending credentials (cookies) with every request
 *
 * All API calls (auth, departments, etc.) must use this instance
 * to ensure consistency and security.
 *
 * Authentication is handled via httpOnly cookies set by the backend,
 * which are automatically sent with each request.
 */
export const api = axios.create({
  /**
   * Base URL of the backend API.
   *
   * - Uses VITE_API_BASE_URL if defined in environment variables
   * - Falls back to localhost for local development
   */
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",

  /**
   * Enable sending cookies with cross-origin requests.
   * Required for httpOnly cookie-based authentication.
   */
  withCredentials: true,
});

export default api;
