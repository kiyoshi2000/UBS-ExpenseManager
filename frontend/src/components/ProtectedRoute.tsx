import { Navigate } from "react-router-dom";

interface ProtectedRouteProps {
  children: JSX.Element;
  allowedRoles?: string[];
}

export const ProtectedRoute = ({ children, allowedRoles }: ProtectedRouteProps) => {
  const token = localStorage.getItem("jwt_token");
  const role = localStorage.getItem("user_role");

  // no token -> goto login
  if (!token) return <Navigate to="/" replace />;

  // no perm -> goto dashboard
  if (allowedRoles && !allowedRoles.includes(role || "")) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

