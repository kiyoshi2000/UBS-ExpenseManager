export interface Department {
  id: number;
  name: string;
  monthlyBudget: number;
  currency: string;
}

export interface DepartmentCreateRequest {
  name: string;
  monthlyBudget: number;
  currency: string;
}