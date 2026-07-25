import { useState } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { loginUser } from "../api/authApi";
import { useAuthStore } from "../store/authStore";
import { Box, Paper, TextField, Button, Typography, Alert, IconButton, Divider } from "@mui/material";
import LoginRoundedIcon from "@mui/icons-material/LoginRounded";
import GoogleIcon from "@mui/icons-material/Google";
import LightModeRoundedIcon from "@mui/icons-material/LightModeRounded";
import DarkModeRoundedIcon from "@mui/icons-material/DarkModeRounded";
import { useThemeStore } from "../store/themeStore";
import { API_BASE_URL } from "../api/apiConfig";
import PasswordField from "../components/PasswordField";

export default function Login() {
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const setAuth = useAuthStore((s) => s.setAuth);
  const mode = useThemeStore((s) => s.mode);
  const toggleTheme = useThemeStore((s) => s.toggle);
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo = location.state?.from?.pathname ?? "/dashboard";

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(""); setLoading(true);
    try {
      const data = await loginUser(form);
      setAuth(data);
      navigate(redirectTo);
    } catch (err) {
      setError(err.response?.data?.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{
      minHeight: "100%", display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "center", gap: 4, px: 2, position: "relative",
    }}>
      <IconButton
        onClick={toggleTheme}
        sx={{ position: "absolute", top: 16, right: 16, color: "var(--color-ink-soft)" }}
      >
        {mode === "light" ? <DarkModeRoundedIcon fontSize="small" /> : <LightModeRoundedIcon fontSize="small" />}
      </IconButton>

      <Box sx={{ textAlign: "center" }}>
        <Typography sx={{ fontFamily: "'Fraunces', serif", fontWeight: 600, fontSize: "2.2rem" }}>
          Split<span style={{ color: "var(--color-brass)" }}>Ledger</span>
        </Typography>
        <Typography sx={{ color: "var(--color-ink-soft)", mt: 0.5 }}>
          Every shared expense, settled fairly.
        </Typography>
      </Box>

      <Paper
        component="form"
        onSubmit={handleSubmit}
        elevation={0}
        sx={{
          width: 380, p: 4, borderRadius: 3,
          border: "1px solid var(--color-rule)",
          borderTop: "3px solid var(--color-brass)",
          boxShadow: "0 4px 24px rgba(27,36,48,0.08)",
          display: "flex", flexDirection: "column", gap: 2,
        }}
      >
        <Typography variant="h5" sx={{ textAlign: "center" }}>Welcome back</Typography>
        {error && <Alert severity="error">{error}</Alert>}
        <TextField name="email" type="email" label="Email" value={form.email} onChange={handleChange} required fullWidth />
        <PasswordField label="Password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />
        <Button type="submit" variant="contained" color="primary" size="large" disabled={loading} startIcon={<LoginRoundedIcon />}>
          {loading ? "Logging in..." : "Log In"}
        </Button>

        <Divider sx={{ my: 0.5 }}>or</Divider>

        <Button
          variant="outlined"
          size="large"
          startIcon={<GoogleIcon />}
          onClick={() => { window.location.href = `${API_BASE_URL}/oauth2/authorization/google`; }}
          sx={{
            borderColor: "var(--color-rule)", color: "var(--color-ink)",
            "&:hover": { borderColor: "var(--color-brass)", bgcolor: "rgba(184,137,43,0.06)" },
          }}
        >
          Continue with Google
        </Button>

        <Typography sx={{ textAlign: "center", fontSize: "0.85rem" }}>
          <Link to="/forgot-password" style={{ color: "var(--color-ink-soft)" }}>Forgot password?</Link>
        </Typography>

        <Typography sx={{ textAlign: "center", color: "var(--color-ink-soft)", fontSize: "0.9rem" }}>
          No account? <Link to="/register" state={location.state} style={{ fontWeight: 600 }}>Register</Link>
        </Typography>
      </Paper>
    </Box>
  );
}

