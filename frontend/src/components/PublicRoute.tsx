import { Navigate } from "react-router-dom";

interface PublicRouteProps {
  children: JSX.Element;
}

export const PublicRoute = ({ children }: PublicRouteProps) => {
  const token = localStorage.getItem("jwt_token");

  // if the user has a session redirect to dashboard
  if (token) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

