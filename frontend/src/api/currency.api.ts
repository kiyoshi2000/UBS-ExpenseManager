import api from "@/services/api";
import type { Currency } from "@/types/department";

/**
 * GET /api/currencies
 * Fetch all available currencies
 */
export async function getCurrencies(): Promise<Currency[]> {
  const response = await api.get<Currency[]>("/api/currencies");
  return response.data;
}

/**
 * GET /api/currencies/{id}
 * Fetch a specific currency by ID
 */
export async function getCurrencyById(id: number): Promise<Currency> {
  const response = await api.get<Currency>(`/api/currencies/${id}`);
  return response.data;
}
