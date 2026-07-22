import { useState } from "react";
import { Link } from "react-router-dom";
import { forgotPassword } from "../api/authApi";
import { Box, Paper, TextField, Button, Typography, Alert } from "@mui/material";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [sent, setSent] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await forgotPassword(email);
    } finally {
      // Always show the same message whether or not the email exists —
      // this endpoint deliberately never reveals which emails are registered.
      setSent(true);
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
        <Typography variant="h5" sx={{ textAlign: "center" }}>Forgot password</Typography>

        {sent ? (
          <Alert severity="success">
            If an account exists for that email, a reset link has been sent.
          </Alert>
        ) : (
          <>
            <Typography color="text.secondary" sx={{ fontSize: "0.9rem" }}>
              Enter your email and we'll send a link to reset your password.
            </Typography>
            <TextField type="email" label="Email" value={email} onChange={(e) => setEmail(e.target.value)} required fullWidth />
            <Button type="submit" variant="contained" size="large" disabled={loading}>
              {loading ? "Sending..." : "Send Reset Link"}
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
