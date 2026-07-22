import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuthStore } from "../store/authStore";
import { Box, Typography, CircularProgress } from "@mui/material";

/**
 * The backend's OAuth2LoginSuccessHandler redirects here after Google login,
 * with our own JWT access/refresh tokens as query params (not Google's tokens —
 * we never expose those to the frontend). We just move them into the same
 * authStore a normal email/password login would use, so the rest of the app
 * can't tell the difference between the two login methods.
 */
export default function OAuth2Redirect() {
  const [params] = useSearchParams();
  const setAuth = useAuthStore((s) => s.setAuth);
  const navigate = useNavigate();

  useEffect(() => {
    const accessToken = params.get("accessToken");
    const refreshToken = params.get("refreshToken");
    const name = params.get("name");
    const email = params.get("email");

    if (accessToken && refreshToken) {
      setAuth({ accessToken, refreshToken, name, email });
      navigate("/dashboard", { replace: true });
    } else {
      navigate("/login", { replace: true });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Box sx={{ minHeight: "100%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 2 }}>
      <CircularProgress sx={{ color: "var(--color-brass)" }} />
      <Typography color="text.secondary">Signing you in...</Typography>
    </Box>
  );
}