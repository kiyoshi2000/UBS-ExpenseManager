export interface Currency {
  id: number;
  name: string;
  exchangeRate: number;
}

export interface Department {
  id: number;
  name: string;
  monthlyBudget: number;
  dailyBudget: number | null; 
  currencyId: number;
  currencyName: string;
}

export interface DepartmentCreateRequest {
  name: string;
  monthlyBudget: number;
  dailyBudget?: number | null; 
  currencyId: number;
}
