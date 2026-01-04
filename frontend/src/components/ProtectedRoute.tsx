import { Navigate } from "react-router-dom";

interface User {
  id: number;
  email: string;
  role: string;
}

interface ProtectedRouteProps {
  children: JSX.Element;
  allowedRoles?: string[];
}

const getUser = (): User | null => {
  const raw = localStorage.getItem("user");
  if (!raw) return null;

  try {
    return JSON.parse(raw) as User;
  } catch {
    return null;
  }
};

export const ProtectedRoute = ({ children, allowedRoles }: ProtectedRouteProps) => {
  const user = getUser();

  // no user -> goto login
  if (!user) return <Navigate to="/" replace />;

  // no perm -> goto dashboard
  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

