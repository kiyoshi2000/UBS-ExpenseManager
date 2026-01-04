import api from "@/services/api";
import { PageableResponse } from "@/types/pagination";

export interface User {
  id: number;
  email: string;
  role: string;
  name: string;
  active: boolean;
  manager?: {
    id: number;
    name: string;
    email: string;
  };
  department?: {
    id: number;
    name: string;
  };
}

export interface CreateUserPayload {
  email: string;
  password: string;
  name: string;
  role: string;
  managerEmail?: string;
  departmentId?: number;
}

export interface UpdateUserPayload {
  email: string;
  password: string;
  role: string;
  name: string;
  managerEmail?: string;
  departmentId?: number;
}

/**
 * Fetch paginated list of users with optional filters
 */
export async function getUsers(params: {
  page?: number;
  size?: number;
  sort?: string | string[];
  search?: string;
  includeInactive?: boolean;
  departmentId?: number;
  role?: string;
}): Promise<PageableResponse<User>> {
  const queryParams = new URLSearchParams();
  
  if (params.page !== undefined) queryParams.append("page", params.page.toString());
  if (params.size !== undefined) queryParams.append("size", params.size.toString());
  if (params.sort) {
    if (Array.isArray(params.sort)) {
      params.sort.forEach(s => queryParams.append("sort", s));
    } else {
      queryParams.append("sort", params.sort);
    }
  }
  if (params.search) queryParams.append("search", params.search);
  if (params.includeInactive !== undefined) queryParams.append("includeInactive", params.includeInactive.toString());
  if (params.departmentId !== undefined) queryParams.append("departmentId", params.departmentId.toString());
  if (params.role) queryParams.append("role", params.role);

  const response = await api.get<PageableResponse<User>>(`/api/users?${queryParams.toString()}`);
  return response.data;
}

/**
 * Get managers by department
 */
export async function getManagersByDepartment(departmentId: number): Promise<User[]> {
  const response = await api.get<PageableResponse<User>>(
    `/api/users?departmentId=${departmentId}&role=MANAGER&size=1000`
  );
  return response.data.content || response.data;
}

export async function createUser(payload: CreateUserPayload): Promise<User> {
  const response = await api.post<User>("/api/auth/register", payload);
  return response.data;
}

export async function updateUser(userId: number, payload: UpdateUserPayload): Promise<User> {
  const response = await api.put<User>(`/api/users/${userId}`, payload);
  return response.data;
}

/**
 * Deactivate a user (soft delete)
 */
export async function deactivateUser(userId: number): Promise<void> {
  await api.delete(`/api/users/${userId}`);
}

export async function reactivateUser(userId: number): Promise<void> {
  await api.patch(`/api/users/${userId}/reactivate`, {});
}
