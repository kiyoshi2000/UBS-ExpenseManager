import type { UserRole } from '@/config/navigation';
import { api } from '@/services/api';
import { useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';

export interface User {
  id: number;
  email: string;
  role: UserRole;
  name?: string;
}

const STORAGE_KEYS = {
  USER: 'user',
} as const;

const parseUser = (raw: string | null): User | null => {
  if (!raw) return null;

  try {
    return JSON.parse(raw) as User;
  } catch {
    console.error('Failed to parse user from localStorage');
    return null;
  }
};

export const useAuth = () => {
  const navigate = useNavigate();

  const user = useMemo(() => {
    return parseUser(localStorage.getItem(STORAGE_KEYS.USER));
  }, []);

  const isAuthenticated = useMemo(() => !!user, [user]);

  const logout = useCallback(async () => {
    try {
      await api.post('/api/auth/logout');
    } catch (error) {
      console.error('Logout request failed:', error);
    } finally {
      localStorage.removeItem(STORAGE_KEYS.USER);
      navigate('/');
    }
  }, [navigate]);

  const hasRole = useCallback(
    (role: UserRole): boolean => user?.role === role,
    [user]
  );

  const hasAnyRole = useCallback(
    (roles: UserRole[]): boolean => (user ? roles.includes(user.role) : false),
    [user]
  );

  return {
    user,
    isAuthenticated,
    logout,
    hasRole,
    hasAnyRole,
  };
};
