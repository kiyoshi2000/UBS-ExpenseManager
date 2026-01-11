import api from "@/services/api";
import { Department } from "../types/department";

export interface DepartmentPayload {
  name: string;
  monthlyBudget: number;
  dailyBudget?: number;
  currencyId: number;
}

/**
 * GET /api/departments
 */
export async function getDepartments(): Promise<Department[]> {
  const response = await api.get<Department[]>("/api/departments");
  return response.data;
}

/**
 * POST /api/departments
 */
export async function createDepartment(
  payload: DepartmentPayload
): Promise<Department> {
  const response = await api.post<Department>("/api/departments", payload);
  return response.data;
}

/**
 * PUT /api/departments/{id}
 */
export async function updateDepartment(
  id: number,
  payload: DepartmentPayload
): Promise<Department> {
  const response = await api.put<Department>(
    `/api/departments/${id}`,
    payload
  );
  return response.data;
}

/**
 * DELETE /api/departments/{id}
 */
export async function deleteDepartment(id: number): Promise<void> {
  await api.delete(`/api/departments/${id}`);
}
