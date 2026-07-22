import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuthStore } from "../store/authStore";

export default function ProtectedRoute() {
  const accessToken = useAuthStore((state) => state.accessToken);
  const location = useLocation();

  // Remember where the user was actually trying to go (e.g. /join/:token)
  // so Login/Register can send them back there instead of always /dashboard.
  return accessToken ? <Outlet /> : <Navigate to="/login" state={{ from: location }} replace />;
}