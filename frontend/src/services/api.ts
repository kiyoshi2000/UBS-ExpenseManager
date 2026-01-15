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
   * - In development: Uses BACKEND_ENDPOINT env var or localhost
   * - In production: Uses relative path (proxied through Vercel rewrites)
   *
   * This avoids cross-site cookie issues in Chrome/Safari by making
   * API calls same-origin in production.
   */
  baseURL: import.meta.env.DEV
    ? (import.meta.env.BACKEND_ENDPOINT ?? "http://localhost:8080")
    : "",

  /**
   * Enable sending cookies with cross-origin requests.
   * Required for httpOnly cookie-based authentication.
   */
  withCredentials: true,
});

export default api;
