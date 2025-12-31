import type { UserRole } from '@/config/navigation';
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
  TOKEN: 'jwt_token',
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
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
    if (!token) return null;
    
    return parseUser(localStorage.getItem(STORAGE_KEYS.USER));
  }, []);

  const isAuthenticated = useMemo(() => !!user, [user]);

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEYS.TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
    navigate('/');
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
