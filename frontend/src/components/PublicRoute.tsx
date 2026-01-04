import { Navigate } from "react-router-dom";

interface PublicRouteProps {
  children: JSX.Element;
}

export const PublicRoute = ({ children }: PublicRouteProps) => {
  const user = localStorage.getItem("user");

  // if the user has a session redirect to dashboard
  if (user) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

