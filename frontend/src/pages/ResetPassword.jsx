import { useState } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { resetPassword } from "../api/authApi";
import { Box, Paper, TextField, Button, Typography, Alert } from "@mui/material";

export default function ResetPassword() {
  const [params] = useSearchParams();
  const [newPassword, setNewPassword] = useState("");
  const [error, setError] = useState("");
  const [done, setDone] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const token = params.get("token");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(""); setLoading(true);
    try {
      await resetPassword(token, newPassword);
      setDone(true);
      setTimeout(() => navigate("/login"), 2000);
    } catch (err) {
      setError(err.response?.data?.message || "Could not reset password. The link may have expired.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ minHeight: "100%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 4, px: 2 }}>
      <Box sx={{ textAlign: "center" }}>
        <Typography sx={{ fontFamily: "'Fraunces', serif", fontWeight: 600, fontSize: "2.2rem" }}>
          Split<span style={{ color: "var(--color-brass)" }}>Ledger</span>
        </Typography>
      </Box>

      <Paper
        component="form" onSubmit={handleSubmit} elevation={0}
        sx={{
          width: 380, p: 4, borderRadius: 3, border: "1px solid var(--color-rule)",
          borderTop: "3px solid var(--color-brass)", boxShadow: "0 4px 24px rgba(27,36,48,0.08)",
          display: "flex", flexDirection: "column", gap: 2,
        }}
      >
        <Typography variant="h5" sx={{ textAlign: "center" }}>Reset password</Typography>

        {!token && <Alert severity="error">This link is missing a token.</Alert>}
        {error && <Alert severity="error">{error}</Alert>}
        {done && <Alert severity="success">Password updated. Redirecting to login...</Alert>}

        {!done && token && (
          <>
            <TextField
              type="password" label="New password" value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)} required fullWidth
              helperText="At least 6 characters"
            />
            <Button type="submit" variant="contained" size="large" disabled={loading}>
              {loading ? "Updating..." : "Update Password"}
            </Button>
          </>
        )}

        <Typography sx={{ textAlign: "center", fontSize: "0.9rem", color: "var(--color-ink-soft)" }}>
          <Link to="/login" style={{ fontWeight: 600 }}>Back to login</Link>
        </Typography>
      </Paper>
    </Box>
  );
}
