import api from "./api";
import { Department, DepartmentCreateRequest } from "@/types/department";

export function listDepartments() {
  return api.get<Department[]>("/api/departments");
}

export function createDepartment(data: DepartmentCreateRequest) {
  return api.post<Department>("/api/departments", data);
}

export function updateDepartment(id: number, data: DepartmentCreateRequest) {
  return api.put<Department>(`/api/departments/${id}`, data);
}

export function deleteDepartment(id: number) {
  return api.delete(`/api/departments/${id}`);
}
