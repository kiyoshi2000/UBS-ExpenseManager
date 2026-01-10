export interface Department {
  id: number;
  name: string;
  monthlyBudget: number;
  dailyBudget: number | null; 
  currency: "USD" | "BRL";
}

export interface DepartmentCreateRequest {
  name: string;
  monthlyBudget: number;
  dailyBudget?: number | null; 
  currency: "USD" | "BRL";
}
