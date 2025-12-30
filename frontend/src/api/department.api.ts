import { Department } from '../types/department'

export async function getDepartments(): Promise<Department[]> {
  const response = await fetch('/api/departments')

  if (!response.ok) {
    throw new Error('Failed to fetch departments')
  }

  return response.json()
}