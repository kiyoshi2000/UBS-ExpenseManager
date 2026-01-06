import { Department } from "../types/department";

export interface DepartmentPayload {
  name: string;
  monthlyBudget: number;
  dailyBudget?: number;
  currency: "USD" | "BRL";
}

/**
 * Centralized response handler
 */
async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || "Unexpected API error");
  }
  return response.json();
}

/**
 * GET /api/departments
 */
export async function getDepartments(): Promise<Department[]> {
  const response = await fetch("/api/departments");
  return handleResponse<Department[]>(response);
}

/**
 * POST /api/departments
 */
export async function createDepartment(
  payload: DepartmentPayload
): Promise<Department> {
  const response = await fetch("/api/departments", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  return handleResponse<Department>(response);
}

/**
 * PUT /api/departments/{id}
 */
export async function updateDepartment(
  id: number,
  payload: DepartmentPayload
): Promise<Department> {
  const response = await fetch(`/api/departments/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  return handleResponse<Department>(response);
}

/**
 * DELETE /api/departments/{id}
 */
export async function deleteDepartment(id: number): Promise<void> {
  const response = await fetch(`/api/departments/${id}`, {
    method: "DELETE",
  });

  if (!response.ok) {
    throw new Error("Failed to delete department");
  }
}
