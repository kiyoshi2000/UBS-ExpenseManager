import api from "@/services/api";
import { PageableResponse } from "@/types/pagination";

export interface Category {
  id: number;
  name: string;
  dailyBudget: number;
  monthlyBudget: number;
  currencyName: string;
  active: boolean;
}

/** Payloads for create/update */
export interface CreateCategoryPayload {
  name: string;
  dailyBudget: number;
  monthlyBudget: number;
  currencyName: string;
}

export interface UpdateCategoryPayload {
  name: string;
  dailyBudget: number;
  monthlyBudget: number;
  currencyName: string;
}

/**
 * Fetch paginated list of categories with optional search
 */
export async function getCategories(params: {
  page?: number;
  size?: number;
  sort?: string | string[];
  search?: string;
  includeInactive?: boolean;
}): Promise<PageableResponse<Category>> {
  const queryParams = new URLSearchParams();

  if (params.page !== undefined)
    queryParams.append("page", params.page.toString());

  if (params.size !== undefined)
    queryParams.append("size", params.size.toString());

  if (params.sort) {
    if (Array.isArray(params.sort)) {
      params.sort.forEach((s) =>
        queryParams.append("sort", s)
      );
    } else {
      queryParams.append("sort", params.sort);
    }
  }

  if (params.search)
    queryParams.append("search", params.search);

  if (params.includeInactive !== undefined)
    queryParams.append(
      "includeInactive",
      params.includeInactive.toString()
    );

  const response = await api.get<PageableResponse<Category>>(
    `/api/expense-categories?${queryParams.toString()}`
  );

  return response.data;
}

/**
 * Create a new category
 */
export async function createCategory(
  payload: CreateCategoryPayload
): Promise<Category> {
  const response = await api.post<Category>(
    "/api/expense-categories",
    payload
  );
  return response.data;
}

/**
 * Update an existing category
 */
export async function updateCategory(
  categoryId: number,
  payload: UpdateCategoryPayload
): Promise<Category> {
  const response = await api.put<Category>(
    `/api/expense-categories/${categoryId}`,
    payload
  );
  return response.data;
}
